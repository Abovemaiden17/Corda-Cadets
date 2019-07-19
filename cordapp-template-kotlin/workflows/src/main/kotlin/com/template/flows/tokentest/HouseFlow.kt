package com.template.flows.tokentest

import co.paralleluniverse.fibers.Suspendable
import com.r3.corda.lib.tokens.contracts.states.FungibleToken
import com.r3.corda.lib.tokens.workflows.flows.move.addMoveTokens
import com.r3.corda.lib.tokens.workflows.internal.flows.distribution.UpdateDistributionListFlow
import com.r3.corda.lib.tokens.workflows.internal.flows.finality.ObserverAwareFinalityFlow
import com.r3.corda.lib.tokens.workflows.internal.flows.finality.ObserverAwareFinalityFlowHandler
import com.r3.corda.lib.tokens.workflows.internal.selection.TokenSelection
import com.r3.corda.lib.tokens.workflows.types.PartyAndAmount
import com.r3.corda.lib.tokens.workflows.utilities.getPreferredNotary
import com.r3.corda.lib.tokens.workflows.utilities.ourSigningKeys
import net.corda.confidential.IdentitySyncFlow
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.unwrap

@StartableByRPC
@InitiatingFlow
class HouseFlow(val house:HouseState, val newOwner: Party ): FlowLogic<SignedTransaction>()
{
    @Suspendable
    override fun call(): SignedTransaction {
        val txBuilder = TransactionBuilder(notary = getPreferredNotary(serviceHub))
        val session = initiateFlow(newOwner)
        session.send(PriceNotification(house.valuation))
        val input = subFlow(ReceiveStateAndRefFlow<FungibleToken>(session))
        val output = session.receive<List<FungibleToken>>().unwrap{it}
        addMoveTokens(txBuilder,input,output)
        subFlow(IdentitySyncFlow.Send(session,txBuilder.toWireTransaction(serviceHub)))
        val ourSigningKeys = txBuilder.toLedgerTransaction(serviceHub).ourSigningKeys(serviceHub)
        val initialstx = serviceHub.signInitialTransaction(txBuilder,signingPubKeys = ourSigningKeys)
        val stx = subFlow(CollectSignaturesFlow(initialstx, listOf(session),ourSigningKeys))
        subFlow(UpdateDistributionListFlow(stx))
        return subFlow(ObserverAwareFinalityFlow(stx, listOf(session)))
    }
}
@InitiatedBy(HouseFlow::class)
class HouseFlowResponder(val othersidesession: FlowSession): FlowLogic<Unit>() {
    @Suspendable
    override fun call() {
        val pricenotif = othersidesession.receive<PriceNotification>().unwrap { it }
        val changeholder = serviceHub.keyManagementService.freshKeyAndCert(ourIdentityAndCert, false).party.anonymise()
        val (input, output) = TokenSelection(serviceHub).generateMove(
                lockId = runId.uuid,
                partyAndAmounts = listOf(PartyAndAmount(othersidesession.counterparty, pricenotif.amount)),
                changeHolder = changeholder
        )
        subFlow(SendStateAndRefFlow(othersidesession,input))
        othersidesession.send(output)
        subFlow(IdentitySyncFlow.Receive(othersidesession))
        subFlow(object : SignTransactionFlow(othersidesession)
        {
            override fun checkTransaction(stx: SignedTransaction) {

            }
        })
        subFlow(ObserverAwareFinalityFlowHandler(othersidesession))
    }
}