package quantumtoken.contracts

import com.r3.corda.lib.tokens.contracts.EvolvableTokenContract
import net.corda.core.contracts.Amount
import net.corda.core.contracts.Contract
import net.corda.core.transactions.LedgerTransaction
import quantumtoken.states.TestState

class TestContract : EvolvableTokenContract(), Contract
{
    companion object
    {
        @JvmStatic
        val TEST_ID = "quantumtoken.contracts.TestContract"
    }

    override fun additionalCreateChecks(tx: LedgerTransaction) {
        val newHouse = tx.outputStates.single() as TestState
        newHouse.apply {
            require(valuation > Amount.zero(valuation.token)) { "Valuation must be greater than zero." }
        }
    }

    override fun additionalUpdateChecks(tx: LedgerTransaction) {
        val oldHouse = tx.inputStates.single() as TestState
        val newHouse = tx.outputStates.single() as TestState
        require(oldHouse.address == newHouse.address) { "The address cannot change." }
        require(newHouse.valuation > Amount.zero(newHouse.valuation.token)) { "Valuation must be greater than zero." }
    }
}