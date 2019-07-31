package com.template.flows.DVP

import co.paralleluniverse.fibers.Suspendable
import com.r3.corda.lib.tokens.money.FiatCurrency
import com.r3.corda.lib.tokens.workflows.flows.rpc.MoveFungibleTokens
import com.r3.corda.lib.tokens.workflows.types.PartyAndAmount
import net.corda.core.contracts.Amount
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.transactions.SignedTransaction

@InitiatingFlow
@StartableByRPC
class MoveFungibleToken (val receiver: String , val amount: Long, val crncy: String): FlowFunctions(){

    @Suspendable
    override fun call(): SignedTransaction {

        val recv = stringToParty(receiver)
        val amount = Amount(amount, FiatCurrency.getInstance(crncy))
        val partyAndAmount = listOf(PartyAndAmount(recv,amount))
        return subFlow<Any>(MoveFungibleTokens(partyAndAmount)) as SignedTransaction



    }


}