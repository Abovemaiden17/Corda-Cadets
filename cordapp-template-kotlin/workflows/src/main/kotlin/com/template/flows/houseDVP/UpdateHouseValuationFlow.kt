package com.template.flows.houseDVP

import co.paralleluniverse.fibers.Suspendable
import com.google.common.collect.ImmutableList
import com.r3.corda.lib.tokens.contracts.utilities.amount
import com.r3.corda.lib.tokens.money.FiatCurrency
import com.r3.corda.lib.tokens.workflows.flows.rpc.UpdateEvolvableToken
import com.template.houseDVP.HouseDVPState
import net.corda.core.contracts.Amount
import net.corda.core.flows.StartableByRPC
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import java.util.*

@StartableByRPC
class UpdateHouseValuationFlow (private val houseId: String,
                                private val newAmount: Long,
                                private val newCurrency: String) : HouseDVPFunction() {
    @Suspendable
    override fun call(): SignedTransaction {
        val uuid = UUID.fromString(houseId)
        val queryCriteria = QueryCriteria.LinearStateQueryCriteria(null,
                ImmutableList.of(uuid), null, Vault.StateStatus.UNCONSUMED)
        val input = serviceHub.vaultService.queryBy(HouseDVPState::class.java, queryCriteria).states[0]
        val houseState = input.state.data
        val outputState = HouseDVPState(houseState.owner,
                houseState.fractionDigits,
                houseState.linearId,
                houseState.maintainers,
                valuation = Amount(newAmount, FiatCurrency.getInstance(newCurrency)),
                noOfBedRooms = houseState.noOfBedRooms,
                address = houseState.address)
        return subFlow(UpdateEvolvableToken(input, outputState))
    }
}