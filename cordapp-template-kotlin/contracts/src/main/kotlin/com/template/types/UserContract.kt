package com.template.types

import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.TypeOnlyCommandData
import net.corda.core.transactions.LedgerTransaction

class UserContract : Contract {
    companion object {
        // Used to identify our contract when building a transaction.
        const val ID_Contracts = "com.template.types.UserContract"
    }

    interface Commands : CommandData {
        class Register : TypeOnlyCommandData(), Commands
        class Verify : TypeOnlyCommandData(), Commands
        class Update : TypeOnlyCommandData(), Commands
    }

    override fun verify(tx: LedgerTransaction) {

    }
}