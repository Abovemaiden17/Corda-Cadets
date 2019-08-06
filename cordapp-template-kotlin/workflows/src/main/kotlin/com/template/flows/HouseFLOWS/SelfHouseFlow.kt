package com.template.flows.HouseFLOWS

import co.paralleluniverse.fibers.Suspendable
import com.r3.corda.lib.tokens.contracts.states.FungibleToken
import com.r3.corda.lib.tokens.contracts.states.NonFungibleToken
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
import com.template.TokenSDKsample.House
import net.corda.confidential.IdentitySyncFlow
import net.corda.core.contracts.Amount
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.unwrap



@StartableByRPC
@InitiatingFlow
class SellHouseFlow(val linearId: UniqueIdentifier, val newOwner: Party) : FlowFunctions() {

    @CordaSerializable
    data class PriceNotification(val amount: Amount<TokenType>)


    @Suspendable
    override fun call(): SignedTransaction {
        val house = inputStateRef(linearId).state.data
        val txBuilder = TransactionBuilder(notary = getPreferredNotary(serviceHub))
        val session = initiateFlow(newOwner)

//        val inputsNF = subFlow(SendTransactionFlow(session,))
//        addMoveTokens(txBuilder, inputsNF, newOwner)
        addMoveNonFungibleTokens(txBuilder, serviceHub, house.toPointer<House>(), newOwner)

        // Ask for input stateAndRefs - send notification with the amount to exchange.
        session.send(PriceNotification(house.valuation))
        val inputs = subFlow(ReceiveStateAndRefFlow<FungibleToken>(session))
        // Receive outputs (this is just quick and dirty, we could calculate them on our side of the flow).
        val outputs = session.receive<List<FungibleToken>>().unwrap { it }
        addMoveTokens(txBuilder, inputs, outputs)
        // Synchronise any confidential identities
        subFlow(IdentitySyncFlow.Send(session, txBuilder.toWireTransaction(serviceHub)))
        val ourSigningKeys = txBuilder.toLedgerTransaction(serviceHub).ourSigningKeys(serviceHub)
        val initialStx = serviceHub.signInitialTransaction(txBuilder, signingPubKeys = ourSigningKeys)
        val stx = subFlow(CollectSignaturesFlow(initialStx, listOf(session), ourSigningKeys))
        // Update distribution list.
        subFlow(UpdateDistributionListFlow(stx))
        return subFlow(ObserverAwareFinalityFlow(stx, listOf(session)))


}


}
    @InitiatedBy(SellHouseFlow::class)
    class SellHouseFlowHandler(val otherSession: FlowSession) : FlowLogic<Unit>() {
        @Suspendable
        override fun call() {

            val priceNotification = otherSession.receive<SellHouseFlow.PriceNotification>().unwrap { it }
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
