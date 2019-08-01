package com.template.flows.tokenSDK

import co.paralleluniverse.fibers.Suspendable
import com.r3.corda.lib.tokens.contracts.states.FungibleToken
import com.r3.corda.lib.tokens.money.FiatCurrency
import com.r3.corda.lib.tokens.workflows.flows.rpc.CreateEvolvableTokens
import net.corda.core.contracts.Amount
import net.corda.core.contracts.FungibleAsset
import net.corda.core.contracts.TransactionState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction

@StartableByRPC
@InitiatingFlow
class CreateHouseToken(private val owner: Party,
                       private val amount: Long,
                       private val currency: String) : FlowLogic<SignedTransaction>() {

    @Suspendable
    override fun call(): SignedTransaction {
        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val state = TransactionState(outputState(), HouseContract.TEST_ID, notary)
        return subFlow(CreateEvolvableTokens(state))
    }

    private fun outputState(): HouseState {
        return HouseState(
                owner = owner,
                address = "myHouse",
                valuation = Amount(amount, FiatCurrency.getInstance(currency)),
                maintainers = listOf(ourIdentity),
                linearId = UniqueIdentifier()
        )
    }
}