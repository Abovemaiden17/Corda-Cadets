package com.template.flows.userwithschedulestate

import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.TypeOnlyCommandData
import net.corda.core.transactions.LedgerTransaction

class RegisterContract : Contract
{
    companion object
    {
        const val CONTRACT_ID = "com.template.flows.userwithschedulestate.RegisterContract"
    }

    override fun verify(tx: LedgerTransaction) {
    }
    interface Commands : CommandData {
        class User :TypeOnlyCommandData(), Commands
    }
}
