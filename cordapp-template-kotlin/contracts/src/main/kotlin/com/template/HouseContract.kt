package com.template

import com.r3.corda.lib.tokens.contracts.EvolvableTokenContract
import com.template.houseDVP.HouseDVPState
import com.template.states.HouseState
import net.corda.core.contracts.Amount
import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.TypeOnlyCommandData
import net.corda.core.transactions.LedgerTransaction

class HouseContract : EvolvableTokenContract(), Contract {
    companion object {
        @JvmStatic
        val HOUSE_CONTRACT_ID = "com.template.HouseContract"
    }

    override fun additionalCreateChecks(tx: LedgerTransaction) {
        val newHouse = tx.outputStates.single() as HouseDVPState
        newHouse.apply {
            require(valuation > Amount.zero(valuation.token)) {
                "Valuation must be greater than zero."
            }
        }
    }
    override fun additionalUpdateChecks(tx: LedgerTransaction) {
        val oldHouse = tx.inputStates.single() as HouseDVPState
        val newHouse = tx.outputStates.single() as HouseDVPState
        require(oldHouse.address == newHouse.address) {
            "The address cannot be change."
        }
        require(newHouse.valuation > Amount.zero(newHouse.valuation.token)) {
            "Valuation must be greater than zero"
        }
    }
}