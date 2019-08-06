package com.template.flows.DVP

import co.paralleluniverse.fibers.Suspendable
import com.r3.corda.lib.tokens.contracts.utilities.heldBy
import com.r3.corda.lib.tokens.contracts.utilities.issuedBy
import com.r3.corda.lib.tokens.money.FiatCurrency
import com.r3.corda.lib.tokens.workflows.flows.rpc.IssueTokens
import net.corda.core.contracts.Amount
import net.corda.core.flows.FlowException
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction


@StartableByRPC

class FiatCurrencyIssueFlow(private val currency: String,
                            private val amount: Long,
                            private val recipient: String) : FlowFunctions() {
    @Suspendable
    @Throws(FlowException::class)
    override fun call(): SignedTransaction {
        val token = Amount(amount,FiatCurrency.getInstance(currency))
        val abstrctToken= token issuedBy ourIdentity heldBy stringToParty(recipient)
        return subFlow(IssueTokens(listOf(abstrctToken)))
}

}