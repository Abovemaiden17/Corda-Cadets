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
import com.template.DVPstateAndContract.HouseState
import net.corda.confidential.IdentitySyncFlow
import net.corda.core.contracts.Amount
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.unwrap

@CordaSerializable
data class PriceNotification(val amount: Amount<TokenType>)

@InitiatingFlow
@StartableByRPC
class HouseSaleFlow(private val houseId: String, private val buyer: String) : FlowFunctions(){

    @Suspendable
    override fun call(): SignedTransaction {

        val house = inputStateRef(stringToLinear(houseId)).state.data
        val txBuilder = TransactionBuilder(notary = getPreferredNotary(serviceHub))
        val session = initiateFlow(stringToParty(buyer))
        addMoveNonFungibleTokens(txBuilder, serviceHub, house.toPointer<HouseState>(), stringToParty(buyer))
        session.send(PriceNotification(house.valuation))

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

}

@InitiatedBy(HouseSaleFlow::class)
class HouseSaleFlowResponder(private val counterpartySession: FlowSession) : FlowLogic<Unit>() {

@Suspendable
override fun call() {

    val priceNotification = counterpartySession.receive<PriceNotification>().unwrap { it }
    val changeHolder = serviceHub.keyManagementService.freshKeyAndCert(ourIdentityAndCert, false).party.anonymise()
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

        }
    })
    subFlow(ObserverAwareFinalityFlowHandler(counterpartySession))
}
}