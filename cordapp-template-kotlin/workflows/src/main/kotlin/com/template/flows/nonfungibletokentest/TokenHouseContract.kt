package com.template.flows.nonfungibletokentest

import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.transactions.LedgerTransaction
import com.r3.corda.lib.tokens.contracts.EvolvableTokenContract
import com.template.flows.tokentest.HouseState
import net.corda.core.contracts.TypeOnlyCommandData

/*
*  HouseContract governs the evolution of HouseState token. Evolvable tokens must extend the EvolvableTokenContract abstract class, it defines the
*  additionalCreateChecks and additionalCreateChecks method to add custom logic to validate while creation adn updation of evolvable tokens respectively.
* */
class TokenHouseContract :  EvolvableTokenContract(), Contract {
    companion object
    {
        const val CONTRACT_ID = "com.template.flows.nonfungibletokentest.TokenHouseContract"
    }

    override fun additionalCreateChecks(tx: LedgerTransaction) {

    }

    override fun additionalUpdateChecks(tx: LedgerTransaction) {

    }
}


