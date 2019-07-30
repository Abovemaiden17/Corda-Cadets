package com.template.flows.houseFlows

import co.paralleluniverse.fibers.Suspendable
import com.r3.corda.lib.tokens.workflows.flows.rpc.CreateEvolvableTokens
import com.template.HouseContract
import com.template.states.HouseState
import net.corda.core.contracts.Amount
import net.corda.core.contracts.TransactionState
import net.corda.core.transactions.SignedTransaction
import net.corda.core.flows.*
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.StartableByRPC
import net.corda.core.contracts.UniqueIdentifier
import com.r3.corda.lib.tokens.money.FiatCurrency


@InitiatingFlow
@StartableByRPC
class CreateEvolvableTokenFlow(private val currency: String,
                               private val amount: Long) : FlowLogic<SignedTransaction>() {

    @Suspendable
    override fun call(): SignedTransaction {
        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val state = TransactionState(outputState(), HouseContract.HOUSE_CONTRACT_ID, notary)
        val transaction = subFlow(CreateEvolvableTokens(state))
        return recordTransactionWithoutOtherParty(transaction)
    }

    private fun outputState() : HouseState {
        return HouseState(
                address = "QuantumCrowd",
                valuation = Amount(amount, FiatCurrency.getInstance(currency)),
                maintainers = listOf(ourIdentity),
                linearId = UniqueIdentifier()
        )
    }

    @Suspendable
    private fun recordTransactionWithoutOtherParty(transaction: SignedTransaction) : SignedTransaction {
        return subFlow(FinalityFlow(transaction, emptyList()))
    }
}


