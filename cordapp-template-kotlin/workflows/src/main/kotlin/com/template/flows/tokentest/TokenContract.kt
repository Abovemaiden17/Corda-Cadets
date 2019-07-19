package com.template.flows.tokentest

import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.TypeOnlyCommandData
import net.corda.core.transactions.LedgerTransaction

class TokenContract : Contract
{
    companion object
    {
        const val CONTRACT_ID = "com.template.flows.tokentest.TokenContract"
    }

    override fun verify(tx: LedgerTransaction) {
    }
    interface Commands : CommandData {
        class Token :TypeOnlyCommandData(), Commands
    }
}