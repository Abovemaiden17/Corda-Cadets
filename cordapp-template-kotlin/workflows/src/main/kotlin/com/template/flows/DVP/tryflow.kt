package com.template.flows.DVP

import co.paralleluniverse.fibers.Suspendable
import com.r3.corda.lib.tokens.contracts.utilities.amount
import com.template.DVPstateAndContract.Amount
import com.template.DVPstateAndContract.trycontract
import com.template.DVPstateAndContract.trystate
import net.corda.core.contracts.TransactionState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.transactions.SignedTransaction

@StartableByRPC
@InitiatingFlow
class tryflow : FlowLogic<Unit>() {
    @Suspendable
    override fun call() {

        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val state = TransactionState(outPUTstate(), trycontract.contractID, notary)
        return
    }


    private fun outPUTstate(): trystate {

        return trystate(base = "",
                rates = Amount("", 0, "", 0),
                date = "",
                linearId = UniqueIdentifier(),
                participants = listOf(ourIdentity))

    }

}