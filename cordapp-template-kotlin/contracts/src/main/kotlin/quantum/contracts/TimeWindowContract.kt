package quantum.contracts

import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.TypeOnlyCommandData
import net.corda.core.transactions.LedgerTransaction

class TimeWindowContract : Contract
{
    companion object
    {
        @JvmStatic
        val TIMEWINDOW_ID = "quantum.contracts.TimeWindowContract"
    }

    override fun verify(tx: LedgerTransaction) {

    }

    interface Commands : CommandData
    {
        class Register : TypeOnlyCommandData(), Commands
        class Verify : TypeOnlyCommandData(), Commands
    }
}