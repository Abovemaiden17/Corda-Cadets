package com.template.flows.nonfungibletokentest

import co.paralleluniverse.fibers.Suspendable
import com.r3.corda.lib.tokens.money.FiatCurrency
import com.r3.corda.lib.tokens.workflows.flows.rpc.UpdateEvolvableToken
import net.corda.core.contracts.Amount
import net.corda.core.flows.FlowException
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.StartableByRPC
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import java.util.*

@StartableByRPC
class UpdateHouseValuationFlow (val linearId: String, val amount: Long,val currency: String): FlowLogic<SignedTransaction>()
{
    @Suspendable
    @Throws(FlowException::class)
    override fun call(): SignedTransaction {
        val uuid = UUID.fromString(linearId)
        val queryCriteria = QueryCriteria.LinearStateQueryCriteria(uuid = listOf(uuid))
        val input = serviceHub.vaultService.queryBy<TokenHouseState>(queryCriteria).states[0]
        val houseState = input.state.data
        val outputState = TokenHouseState(houseState.owner,houseState.address,Amount(amount, FiatCurrency.getInstance(currency)),
                                            houseState.fractionDigits,
                                            houseState.maintainers,
                                            houseState.linearId)
        return subFlow(UpdateEvolvableToken(input,outputState))
    }
}