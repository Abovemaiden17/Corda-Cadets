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
                            private val recipient: Party) : FlowLogic<SignedTransaction>() {

    @Suspendable
    @Throws(FlowException::class)
    override fun call(): SignedTransaction {
        /* Create an instance of the fiat currency token */
        val token = Amount(amount,FiatCurrency.getInstance(currency))
        /* Issue the required amount of the token to the recipient */
        val abstrctToken= token issuedBy ourIdentity heldBy recipient

        return subFlow(IssueTokens(listOf(abstrctToken)))
}

}