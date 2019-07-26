package com.template.flows.tokentest

import co.paralleluniverse.fibers.Suspendable
import com.google.common.collect.ImmutableList
import com.r3.corda.lib.tokens.contracts.states.FungibleToken
import com.r3.corda.lib.tokens.contracts.states.NonFungibleToken
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
import com.r3.corda.lib.tokens.workflows.types.PartyAndToken
import com.r3.corda.lib.tokens.contracts.types.TokenPointer
import com.r3.corda.lib.tokens.contracts.utilities.issuedBy
import com.r3.corda.lib.tokens.money.FiatCurrency
import com.r3.corda.lib.tokens.workflows.flows.move.addMoveNonFungibleTokens
import com.r3.corda.lib.tokens.workflows.utilities.heldBy
import net.corda.core.contracts.Amount
import net.corda.core.contracts.LinearPointer
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.node.services.Vault
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import java.util.*


@StartableByRPC
@InitiatingFlow
class HouseFlow(val linearId: UniqueIdentifier, val newOwner: Party ): FlowLogic<SignedTransaction>()
{
    @Suspendable
    override fun call(): SignedTransaction {
        val uuid = UUID.fromString(linearId.toString())
        val queryCriteria = QueryCriteria.LinearStateQueryCriteria( uuid = ImmutableList.of(uuid))
        val stateAndRef = serviceHub.vaultService.queryBy(HouseState::class.java, queryCriteria).states[0]
        val evolvableTokenType = stateAndRef.state.data
        val linearPointer = LinearPointer(evolvableTokenType.linearId, HouseState::class.java!!)
        val token = TokenPointer(linearPointer, evolvableTokenType.fractionDigits)
        val partyAndToken = PartyAndToken(newOwner, token)
        val criteria = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(linearId))
        val crit = serviceHub.vaultService.queryBy<HouseState>(criteria).states.single()
        val data = crit.state.data
        val txBuilder = TransactionBuilder(notary = getPreferredNotary(serviceHub))
        addMoveNonFungibleTokens(txBuilder,serviceHub,partyAndToken,criteria)
        val session = initiateFlow(newOwner)
        session.send(data.valuation)
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
        val houseState = othersidesession.receive<HouseState>().unwrap{it}
        val changeholder = serviceHub.keyManagementService.freshKeyAndCert(ourIdentityAndCert, false).party.anonymise()
        val (input, output) = TokenSelection(serviceHub).generateMove(
                lockId = runId.uuid,
                partyAndAmounts = listOf(PartyAndAmount(othersidesession.counterparty, Amount(pricenotif.amount,FiatCurrency.getInstance(houseState.address)))),
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