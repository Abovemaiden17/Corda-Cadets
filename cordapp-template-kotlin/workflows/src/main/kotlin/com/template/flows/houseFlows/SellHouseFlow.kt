package com.template.flows.houseFlows

import co.paralleluniverse.fibers.Suspendable
import com.r3.corda.lib.tokens.contracts.states.FungibleToken
import com.r3.corda.lib.tokens.contracts.types.TokenType
import com.r3.corda.lib.tokens.workflows.flows.move.addMoveNonFungibleTokens
import com.r3.corda.lib.tokens.workflows.flows.move.addMoveTokens
import com.r3.corda.lib.tokens.workflows.internal.flows.distribution.UpdateDistributionListFlow
import com.r3.corda.lib.tokens.workflows.internal.flows.finality.ObserverAwareFinalityFlow
import com.r3.corda.lib.tokens.workflows.internal.flows.finality.ObserverAwareFinalityFlowHandler
import com.r3.corda.lib.tokens.workflows.internal.selection.TokenSelection
import com.r3.corda.lib.tokens.workflows.types.PartyAndAmount
import com.r3.corda.lib.tokens.workflows.utilities.ourSigningKeys
import com.template.states.HouseState
import net.corda.confidential.IdentitySyncFlow
import net.corda.core.contracts.Amount
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.unwrap
import java.security.PublicKey

@InitiatingFlow
@StartableByRPC
class SellHouseFlow (private val house: HouseState,
                     private val newOwner: Party): FlowLogic<SignedTransaction>()
{
    override fun call(): SignedTransaction
    {
        val txBuilder = TransactionBuilder(notary = serviceHub.networkMapCache.notaryIdentities.first())
        addMoveNonFungibleTokens(txBuilder, serviceHub,house.toPointer<HouseState>() ,newOwner)
        val session = initiateFlow(newOwner)
        session.send(PriceNotification(house.valuation))
        val inputs = subFlow(ReceiveStateAndRefFlow<FungibleToken>(session))
        val outputs = session.receive<List<FungibleToken>>().unwrap { it }
        addMoveTokens(txBuilder, inputs, outputs)
        subFlow(IdentitySyncFlow.Send(session, txBuilder.toWireTransaction(serviceHub)))
        val ourSigningKeys = txBuilder.toLedgerTransaction(serviceHub).ourSigningKeys(serviceHub)
        val initialStx = verifyAndSign(txBuilder, ourSigningKeys)
        val stx = collectSignature(initialStx, listOf(session), ourSigningKeys)
        subFlow(UpdateDistributionListFlow(stx))
        return subFlow(ObserverAwareFinalityFlow(stx, listOf(session)))
    }

    private fun verifyAndSign(transaction: TransactionBuilder, signingPubKeys: List<PublicKey>): SignedTransaction {
    transaction.verify(serviceHub)
    return serviceHub.signInitialTransaction(transaction, signingPubKeys)
    }

    @Suspendable
    fun collectSignature(
            transaction: SignedTransaction,
            sessions: List<FlowSession>,
            signingPubKeys: List<PublicKey>
    ): SignedTransaction = subFlow(CollectSignaturesFlow(transaction, sessions, signingPubKeys))
}

@CordaSerializable
data class PriceNotification(val amount: Amount<TokenType>)

@InitiatedBy(SellHouseFlow::class)
class SellHouseFlowResponder(private val otherSession: FlowSession): FlowLogic<Unit>()
{
    override fun call() {
        // Receive notification with house price.
        val priceNotification = otherSession.receive<PriceNotification>().unwrap { it }

        // Generate fresh key, possible change outputs will belong to this key.
        val changeHolder = serviceHub.keyManagementService.freshKeyAndCert(ourIdentityAndCert, false).party.anonymise()

        // Chose state and refs to send back.
        val (inputs, outputs) = TokenSelection(serviceHub).generateMove(
                lockId = runId.uuid,
                partyAndAmounts = listOf(PartyAndAmount(otherSession.counterparty, priceNotification.amount)),
                changeHolder = changeHolder
        )
        subFlow(SendStateAndRefFlow(otherSession, inputs))
        otherSession.send(outputs)
        subFlow(IdentitySyncFlow.Receive(otherSession))
        subFlow(object : SignTransactionFlow(otherSession) {
            override fun checkTransaction(stx: SignedTransaction) {
                // We should perform some basic sanity checks before signing the transaction. This step was omitted for simplicity.
            }
        })
        subFlow(ObserverAwareFinalityFlowHandler(otherSession))
    }
}

