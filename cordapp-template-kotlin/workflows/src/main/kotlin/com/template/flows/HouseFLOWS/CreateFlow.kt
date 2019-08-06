package com.template.flows.HouseFLOWS

import co.paralleluniverse.fibers.Suspendable
import com.r3.corda.lib.tokens.money.FiatCurrency
import com.r3.corda.lib.tokens.workflows.flows.rpc.CreateEvolvableTokens
import com.template.TokenSDKsample.House
import com.template.TokenSDKsample.HouseContract
import net.corda.core.contracts.Amount
import net.corda.core.contracts.TransactionState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.transactions.SignedTransaction


@InitiatingFlow
@StartableByRPC
class CreateFlow(private val amount: Long,
                 private val currency: String): FlowFunctions(){


@Suspendable
    override fun call(): SignedTransaction {
        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val state = TransactionState(outputState(), HouseContract.TEST_ID, notary)
        val transaction = subFlow(CreateEvolvableTokens(state))
        return recordTransactionWithoutOtherParty(transaction)
    }
    private fun outputState(): House
    {
        val amountconverted = amount.toString() + "00"
        val amountFinal = amountconverted.toLong()
        return House(
                "Bahay",
                valuation = Amount(amountFinal, FiatCurrency.getInstance(currency)),
//                issuedAmount = Amount(0, FiatCurrency.getInstance(currency)),
//                wallet = Amount(0,FiatCurrency.getInstance(currency)),
                maintainers = listOf(ourIdentity),
                linearId = UniqueIdentifier()
        )
    }


}