package com.template.flows.tokenSDK

import co.paralleluniverse.fibers.Suspendable
import com.r3.corda.lib.tokens.money.FiatCurrency
import com.r3.corda.lib.tokens.workflows.flows.rpc.MoveFungibleTokens
import com.r3.corda.lib.tokens.workflows.types.PartyAndAmount
import net.corda.core.contracts.Amount
import net.corda.core.flows.FlowException
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction

@StartableByRPC
class MoveFiatCurrency(val currency: String,
                       val amount: Long,
                       val recipient: Party) : FlowLogic<SignedTransaction>() {

    @Suspendable
    @Throws(FlowException::class)
    override fun call(): SignedTransaction {
        val amount = Amount(amount, FiatCurrency.getInstance(currency))
        val partyAndAmount = listOf(PartyAndAmount(recipient,amount))
        return subFlow<Any>(MoveFungibleTokens(partyAndAmount)) as SignedTransaction
    }

}