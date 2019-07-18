package quantum.schedulable

import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.TypeOnlyCommandData
import net.corda.core.transactions.LedgerTransaction

class WalletSchedulableContract : Contract
{
    companion object
    {
        @JvmStatic
        val WALLETSCHEDULE_ID = "quantum.schedulable.WalletSchedulableContract"
    }

    override fun verify(tx: LedgerTransaction) {

    }

    interface Commands : CommandData
    {
        class Register : TypeOnlyCommandData(), Commands
        class SelfIssue : TypeOnlyCommandData(), Commands
    }
}