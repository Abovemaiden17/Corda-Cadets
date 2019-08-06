package com.template.tokenSdk

import com.r3.corda.lib.tokens.contracts.EvolvableTokenContract
import net.corda.core.contracts.Amount
import net.corda.core.contracts.Contract
import net.corda.core.transactions.LedgerTransaction

class HouseContract : EvolvableTokenContract(), Contract {
    companion object
    {
        @JvmStatic
        val ID = "com.template.tokenSdk.HouseContract"
    }

    override fun additionalCreateChecks(tx: LedgerTransaction) {
        // Not much to do for this example token.
        val newHouse = tx.outputStates.single() as HouseState
        newHouse.apply {
            require(valuation > Amount.zero(valuation.token)) { "Valuation must be greater than zero." }
        }
    }

    override fun additionalUpdateChecks(tx: LedgerTransaction) {
        val oldHouse = tx.inputStates.single() as HouseState
        val newHouse = tx.outputStates.single() as HouseState
        require(oldHouse == newHouse) { "The address cannot change." }
        require(newHouse.valuation > Amount.zero(newHouse.valuation.token)) { "Valuation must be greater than zero." }
    }
}
