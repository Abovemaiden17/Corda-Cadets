package com.template.flows.DVP

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
import com.r3.corda.lib.tokens.workflows.utilities.getPreferredNotary
import com.r3.corda.lib.tokens.workflows.utilities.ourSigningKeys
import net.corda.confidential.IdentitySyncFlow
import net.corda.core.contracts.Amount
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.serialization.CordaSerializable
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.unwrap


@InitiatingFlow
@StartableByRPC
class HouseSaleFlow(private val houseId: String, private val buyer: Party) : FlowLogic<SignedTransaction>(){

    @CordaSerializable
    data class PriceNotification(val amount: Amount<TokenType>)

    @Suspendable
    override fun call(): SignedTransaction {
//        val notary = serviceHub.networkMapCache.notaryIdentities.first()
//        val uuid = UUID.fromString(houseId)
//        val queryCriteria = QueryCriteria.LinearStateQueryCriteria(null, ImmutableList.of(uuid), null, Vault.StateStatus.UNCONSUMED)
//        val (state) = serviceHub.vaultService.queryBy(HouseState::class.java, queryCriteria).states[0]
//        val houseState = state.data
//        val txBuilder = TransactionBuilder(notary)
//
//        val linearPointer = LinearPointer(houseState.linearId, House::class.java)
//        val token = TokenPointer(linearPointer, houseState.fractionDigits)
//        val partyAndToken = PartyAndToken(buyer, token)
//
//        addMoveNonFungibleTokens(txBuilder, serviceHub, partyAndToken)
//
//        val buyerSession = initiateFlow(buyer)
//        buyerSession.send(PriceNotification(houseState.valuation))
//
//        val inputs = subFlow(ReceiveStateAndRefFlow<FungibleToken>(buyerSession))
//        val outputs = buyerSession.receive<List<FungibleToken>>().unwrap { it }
//        addMoveTokens(txBuilder, inputs, outputs)
//
//        val initialSignedTrnx = serviceHub.signInitialTransaction(txBuilder, ourIdentity.owningKey)
//
//        val signedTransaction = subFlow(CollectSignaturesFlow(initialSignedTrnx, ImmutableList.of(buyerSession)))
//        subFlow(UpdateDistributionListFlow(signedTransaction))
//
//        return subFlow(FinalityFlow(signedTransaction, ImmutableList.of(buyerSession)))
//    }

        val house = inputStateRef(stringToLinear(houseId)).state.data
        val txBuilder = TransactionBuilder(notary = getPreferredNotary(serviceHub))
        val session = initiateFlow(buyer)
        addMoveNonFungibleTokens(txBuilder, serviceHub, house.toPointer<HouseState>(), buyer)

        session.send(PriceNotification(house.valuation))
//        val abstrctToken= token issuedBy ourIdentity heldBy recipient

        val inputs = subFlow(ReceiveStateAndRefFlow<FungibleToken>(session))
        val outputs = session.receive<List<FungibleToken>>().unwrap { it }
        addMoveTokens(txBuilder, inputs, outputs)
        subFlow(IdentitySyncFlow.Send(session, txBuilder.toWireTransaction(serviceHub)))
        val ourSigningKeys = txBuilder.toLedgerTransaction(serviceHub).ourSigningKeys(serviceHub)
        val initialStx = serviceHub.signInitialTransaction(txBuilder, signingPubKeys = ourSigningKeys)
        val stx = subFlow(CollectSignaturesFlow(initialStx, listOf(session), ourSigningKeys))
        subFlow(UpdateDistributionListFlow(stx))
        return subFlow(ObserverAwareFinalityFlow(stx, listOf(session)))


    }


    fun inputStateRef(id: UniqueIdentifier): StateAndRef<HouseState> {
        val criteria = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(id))
        return serviceHub.vaultService.queryBy<HouseState>(criteria = criteria).states.single()
    }
    fun stringToLinear(id: String): UniqueIdentifier {
        return UniqueIdentifier.fromString(id)
    }


}

@InitiatedBy(HouseSaleFlow::class)
class HouseSaleFlowResponder(private val counterpartySession: FlowSession) : FlowLogic<Unit>() {

@Suspendable
override fun call() {

    val priceNotification = counterpartySession.receive<HouseSaleFlow.PriceNotification>().unwrap { it }
    // Generate fresh key, possible change outputs will belong to this key.
    val changeHolder = serviceHub.keyManagementService.freshKeyAndCert(ourIdentityAndCert, false).party.anonymise()
    // Chose state and refs to send back.
    val (inputs, outputs) = TokenSelection(serviceHub).generateMove(
            lockId = runId.uuid,
            partyAndAmounts = listOf(PartyAndAmount(counterpartySession.counterparty, priceNotification.amount)),
            changeHolder = changeHolder
    )
    subFlow(SendStateAndRefFlow(counterpartySession, inputs))
    counterpartySession.send(outputs)
    subFlow(IdentitySyncFlow.Receive(counterpartySession))
    subFlow(object : SignTransactionFlow(counterpartySession) {
        override fun checkTransaction(stx: SignedTransaction) {
            // We should perform some basic sanity checks before signing the transaction. This step was omitted for simplicity.
        }
    })
    subFlow(ObserverAwareFinalityFlowHandler(counterpartySession))
}
}