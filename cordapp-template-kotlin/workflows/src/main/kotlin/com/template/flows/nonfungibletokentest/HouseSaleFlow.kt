package com.template.flows.nonfungibletokentest

import co.paralleluniverse.fibers.Suspendable
import com.google.common.collect.ImmutableList
import com.r3.corda.lib.tokens.contracts.states.FungibleToken
import com.r3.corda.lib.tokens.workflows.flows.move.addMoveNonFungibleTokens
import com.r3.corda.lib.tokens.workflows.flows.move.addMoveTokens
import com.r3.corda.lib.tokens.workflows.internal.flows.distribution.UpdateDistributionListFlow
import com.r3.corda.lib.tokens.workflows.internal.flows.finality.ObserverAwareFinalityFlow
import com.r3.corda.lib.tokens.workflows.internal.flows.finality.ObserverAwareFinalityFlowHandler
import com.r3.corda.lib.tokens.workflows.internal.selection.TokenSelection
import com.r3.corda.lib.tokens.workflows.types.PartyAndAmount
import com.r3.corda.lib.tokens.workflows.utilities.ourSigningKeys
import net.corda.confidential.IdentitySyncFlow
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.unwrap
import java.util.*


/**
 * Initiator Flow class to propose the sale of the house. The house token would be exchanged with an equivalent amount of fiat currency as mentioned in the
 * valuation of the house. The flow taken the linearId of the house token and the buyer party as the input parameters.
 */
@InitiatingFlow
@StartableByRPC
class HouseSaleFlow(val linearId: String, val buyer: String) : TokenFunctions() {
    @Suspendable
    @Throws(FlowException::class)
    override fun call(): SignedTransaction {
        val notary = serviceHub.networkMapCache.notaryIdentities[0]
        val uuid = UUID.fromString(linearId)
        val queryCriteria = QueryCriteria.LinearStateQueryCriteria(uuid = ImmutableList.of(uuid))
        val houseStateAndRef = serviceHub.vaultService.queryBy<TokenHouseState>(queryCriteria).states[0]
        val houseState = houseStateAndRef.state.data
        val transactionBuilder = TransactionBuilder(notary)
        addMoveNonFungibleTokens(transactionBuilder,serviceHub,houseState.toPointer<TokenHouseState>(),stringtoParty(buyer))
        val buySession = initiateFlow(stringtoParty(buyer))
        buySession.send(PriceNotification(houseState.valuation))
        val inputs = subFlow(ReceiveStateAndRefFlow<FungibleToken>(buySession))
        val outputs = buySession.receive<List<FungibleToken>>().unwrap{it}
        addMoveTokens(transactionBuilder,inputs, outputs)
        subFlow(IdentitySyncFlow.Send(buySession, transactionBuilder.toWireTransaction(serviceHub)))
        val ourSigningKeys = transactionBuilder.toLedgerTransaction(serviceHub).ourSigningKeys(serviceHub)
        val initialStx = serviceHub.signInitialTransaction(transactionBuilder, signingPubKeys = ourSigningKeys)
        val stx = subFlow(CollectSignaturesFlow(initialStx, listOf(buySession), ourSigningKeys))
        subFlow(UpdateDistributionListFlow(stx))
        return subFlow(ObserverAwareFinalityFlow(stx, listOf(buySession)))
    }


}
@InitiatedBy(HouseSaleFlow::class)
class HouseSaleResponderFlow(private val counterpartySession: FlowSession) : FlowLogic<Unit>() {
    @Suspendable
    override fun call() {

        val priceNotification = counterpartySession.receive<PriceNotification>().unwrap { it }
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
