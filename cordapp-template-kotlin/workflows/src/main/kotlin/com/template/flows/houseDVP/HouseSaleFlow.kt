package com.template.flows.houseDVP

import co.paralleluniverse.fibers.Suspendable
import com.google.common.collect.ImmutableList
import com.r3.corda.lib.tokens.contracts.states.FungibleToken
import com.r3.corda.lib.tokens.contracts.types.TokenType
import com.r3.corda.lib.tokens.workflows.flows.move.addMoveNonFungibleTokens
import com.r3.corda.lib.tokens.workflows.flows.move.addMoveTokens
import com.r3.corda.lib.tokens.workflows.internal.flows.distribution.UpdateDistributionListFlow
import com.r3.corda.lib.tokens.workflows.internal.flows.finality.ObserverAwareFinalityFlow
import com.r3.corda.lib.tokens.workflows.internal.selection.TokenSelection
import com.r3.corda.lib.tokens.workflows.types.PartyAndAmount
import com.r3.corda.lib.tokens.workflows.utilities.ourSigningKeys
import com.template.houseDVP.HouseDVPState
import net.corda.confidential.IdentitySyncFlow
import net.corda.core.contracts.Amount
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.serialization.CordaSerializable
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.unwrap
import java.util.*

@InitiatingFlow
@StartableByRPC

class HouseSaleFlow (private val houseId: String,
                     private val buyer: String) : HouseDVPFunction() {
    @Suspendable
    override fun call(): SignedTransaction {
        val notary = serviceHub.networkMapCache.notaryIdentities[0]
        val queryCriteria = QueryCriteria.LinearStateQueryCriteria(null,
                ImmutableList.of(stringToUUID(houseId)), null, Vault.StateStatus.UNCONSUMED)
        val state = serviceHub.vaultService.queryBy(HouseDVPState::class.java, queryCriteria).states[0]
        val houseState = state.state.data

        val txBuilder = TransactionBuilder(notary)
        addMoveNonFungibleTokens(txBuilder, serviceHub,houseState.pointer(),stringToParty(buyer))
        val session = initiateFlow(stringToParty(buyer))
        session.send(houseState.valuation)
        val inputs = subFlow(ReceiveStateAndRefFlow<FungibleToken>(session))
        val outputs = session.receive<List<FungibleToken>>().unwrap { it }
        addMoveTokens(txBuilder, inputs, outputs)
        subFlow(IdentitySyncFlow.Send(session, txBuilder.toWireTransaction(serviceHub)))

        val initialStx = serviceHub.signInitialTransaction(txBuilder, ourIdentity.owningKey)
        val stx = subFlow(CollectSignaturesFlow(initialStx, ImmutableList.of(session)))
        subFlow(UpdateDistributionListFlow(stx))
        return subFlow(ObserverAwareFinalityFlow(stx, ImmutableList.of(session)))
    }
}

@CordaSerializable
data class PriceNotification(val amount: Amount<TokenType>)

@InitiatedBy(HouseSaleFlow::class)
class HouseSaleFlowResponder(private val counterPartySession: FlowSession) : FlowLogic<SignedTransaction>() {

    @Suspendable
    override fun call(): SignedTransaction {
        // Receive notification with house price.
        val priceNotification = counterPartySession.receive<PriceNotification>().unwrap { it }

        // Generate fresh key, possible change outputs will belong to this key.
        val changeHolder = serviceHub.keyManagementService.freshKeyAndCert(ourIdentityAndCert, false).party.anonymise()

        // Chose state and refs to send back.
        val inputsAndOutputs = TokenSelection(serviceHub, 8, 100, 2000).generateMove(
                runId.uuid,
                listOf(PartyAndAmount(counterPartySession.counterparty, priceNotification.amount)),
                ourIdentity,
                null
        )
        subFlow(SendStateAndRefFlow(counterPartySession, inputsAndOutputs.first))
        counterPartySession.send(inputsAndOutputs.second)
        subFlow(IdentitySyncFlow.Receive(counterPartySession))
        subFlow(object : SignTransactionFlow(counterPartySession) {
            override fun checkTransaction(stx: SignedTransaction) {
                // We should perform some basic sanity checks before signing the transaction. This step was omitted for simplicity.
            }
        })
         return subFlow(ReceiveFinalityFlow(counterPartySession))
    }
}