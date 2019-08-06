package com.template.increment

import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction

open class IncrementContract : Contract {
    companion object {
        const val contractID = "com.template.increment.IncrementContract"
    }

    interface Commands : CommandData {
        class Increment : Commands
    }

    override fun verify(tx: LedgerTransaction) {
        val command = tx.getCommand<CommandData>(0)
        requireThat {
            when (command.value) {
                is Commands.Increment -> {


                }
            }
        }
    }
}
