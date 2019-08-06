package com.template.DVPstateAndContract

import com.r3.corda.lib.tokens.contracts.EvolvableTokenContract
import net.corda.core.contracts.Amount
import net.corda.core.contracts.Contract
import net.corda.core.transactions.LedgerTransaction

class HouseContract : EvolvableTokenContract(), Contract {


    companion object
    {
        @JvmStatic
        val TEST_ID = "com.template.DVPstateAndContract.HouseContract"
    }
    override fun additionalCreateChecks(tx: LedgerTransaction) {
        val newHouse = tx.outputStates.single() as HouseState
        newHouse.apply {
            require(valuation > Amount.zero(valuation.token)) { "Valuation must be greater than zero." }
        }
    }

    override fun additionalUpdateChecks(tx: LedgerTransaction) {
        val oldHouse = tx.inputStates.single() as HouseState
        val newHouse = tx.outputStates.single() as HouseState
        require(oldHouse.address == newHouse.address) { "The address cannot change." }
        require(newHouse.valuation > Amount.zero(newHouse.valuation.token)) { "Valuation must be greater than zero." }
    }

}