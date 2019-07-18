package com.template.flows.userwithschedulestate

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.Command
import net.corda.core.flows.*
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import java.time.Instant

@InitiatingFlow
@StartableByRPC
@SchedulableFlow
class RegisterFlow : FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call(): SignedTransaction {
        val output = RegisterState(ourIdentity,false, Instant.now(),10)
        val transaction = TransactionBuilder(notary = serviceHub.networkMapCache.notaryIdentities.first()).apply {
            val command = Command(RegisterContract.Commands.User(), ourIdentity.owningKey)
            addCommand(command)
            addOutputState(state = output, contract = RegisterContract.CONTRACT_ID)
        }
        val signedTX = serviceHub.signInitialTransaction(transaction)
        return subFlow(FinalityFlow(signedTX, listOf()))
    }
}

