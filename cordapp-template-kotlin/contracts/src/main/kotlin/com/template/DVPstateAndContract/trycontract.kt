package com.template.DVPstateAndContract

import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.transactions.LedgerTransaction

open class trycontract : Contract {
    companion object {
        const val contractID = "com.template.DVPstateAndContract.trycontract"
    }

    override fun verify(tx: LedgerTransaction) {
        // Omitted for the purpose of this sample.
    }

    interface Commands : CommandData {
        class Beat : Commands
    }
}