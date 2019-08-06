package com.template.flows.DVP

import co.paralleluniverse.fibers.Suspendable
import com.r3.corda.lib.tokens.money.FiatCurrency
import com.r3.corda.lib.tokens.workflows.flows.rpc.CreateEvolvableTokens
import com.template.DVPstateAndContract.HouseContract
import com.template.DVPstateAndContract.HouseState
import net.corda.core.contracts.Amount
import net.corda.core.contracts.TransactionState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction


@StartableByRPC
@InitiatingFlow
class HouseTokenCreateFlow(private val mayAri: String,
                           private val amount: Long,
                           private val currency: String) : FlowFunctions() {
    @Suspendable
    override fun call(): SignedTransaction {

        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val state = TransactionState(outPUTstate(), HouseContract.TEST_ID, notary)
        return subFlow(CreateEvolvableTokens(state))
    }

    private fun outPUTstate(): HouseState {
        return HouseState(
                mayAri = stringToParty(mayAri),
                address = "Bahay",
                valuation = Amount(amount, FiatCurrency.getInstance(currency)),
                maintainers = listOf(ourIdentity),
                linearId = UniqueIdentifier()
        )
    }

}
