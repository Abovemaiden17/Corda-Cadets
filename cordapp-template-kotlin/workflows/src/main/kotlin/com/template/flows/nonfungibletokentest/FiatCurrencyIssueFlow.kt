package com.template.flows.nonfungibletokentest

import co.paralleluniverse.fibers.Suspendable
import com.r3.corda.lib.tokens.contracts.utilities.heldBy
import com.r3.corda.lib.tokens.contracts.utilities.issuedBy
import com.r3.corda.lib.tokens.money.FiatCurrency
import com.r3.corda.lib.tokens.workflows.flows.rpc.IssueTokens
import com.r3.corda.lib.tokens.workflows.utilities.heldBy
import net.corda.core.contracts.Amount
import net.corda.core.flows.FlowException
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction

import java.util.Currency

/**
 * Flow class to issue fiat currency. FiatCurrency is defined in the TokenSDK and is issued as a Fungible Token. This constructor takes the currecy code
 * for the currency to be issued, the amount of the currency to be issued and the recipient as input parameters.
 */
@InitiatingFlow
@StartableByRPC
class FiatCurrencyIssueFlow(val currency: String, val amount: Long, val recipient: Party) : FlowLogic<SignedTransaction>() {

    @Suspendable
    @Throws(FlowException::class)
    override fun call(): SignedTransaction {
        val token = Amount(amount,FiatCurrency.getInstance(currency))
        val abstracttoken = token issuedBy ourIdentity heldBy recipient
        return subFlow(IssueTokens(listOf(abstracttoken)))
    }

}