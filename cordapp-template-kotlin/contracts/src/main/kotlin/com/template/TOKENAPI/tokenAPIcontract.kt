package com.template.TOKENAPI

import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.transactions.LedgerTransaction


class tokenAPIcontract : Contract {
    companion object {
        const val contractID = "com.template.TOKENAPI.tokenAPIcontract"
    }

    override fun verify(tx: LedgerTransaction) {
        // Omitted for the purpose of this sample.
    }

    interface Commands : CommandData {
        class Beat : Commands
    }
}

