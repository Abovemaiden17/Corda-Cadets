package com.template.flows.tokentest

import co.paralleluniverse.fibers.Suspendable
import com.r3.corda.lib.tokens.money.FiatCurrency
import com.r3.corda.lib.tokens.workflows.flows.rpc.CreateEvolvableTokens
import net.corda.core.contracts.Amount
import net.corda.core.contracts.Command
import net.corda.core.contracts.TransactionState
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder

@InitiatingFlow
@StartableByRPC
class CreateTokenFlow (val name: String, val amount: Long,val address: String)
    : FlowLogic<SignedTransaction>()
{
    @Suspendable
    override fun call(): SignedTransaction {
        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val state = TransactionState(input(),TokenContract.CONTRACT_ID,notary)
        val signedTx = subFlow(CreateEvolvableTokens(state))
        return signedTx
    }
    private fun input(): HouseState
    {
        return HouseState(address,Amount(amount,FiatCurrency.getInstance(name)), listOf(ourIdentity))
    }
}