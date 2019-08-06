package com.template.heartBeat

import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction


/**
 * A blank contract and command, solely used for building a valid Heartbeat state transaction.
 */
open class HeartBeatContract : Contract {
    companion object {
        const val contractID = "com.template.heartBeat.HeartBeatContract"
    }

    interface Commands : CommandData {
        class Beat : Commands
    }

    override fun verify(tx: LedgerTransaction) {
        val command = tx.getCommand<CommandData>(0)
        requireThat {
            when (command.value) {
                is Commands.Beat -> {


                }
            }
        }
    }
}



