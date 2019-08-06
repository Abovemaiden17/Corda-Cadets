package com.template.tokenschedule

import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.transactions.LedgerTransaction

open class UserRegisterContract : Contract {
    companion object {
        const val contractID = "com.template.tokenschedule.UserRegisterContract"
    }

    override fun verify(tx: LedgerTransaction) {
        // Omitted for the purpose of this sample.
    }

    interface Commands : CommandData {
        class Register : Commands
    }
}