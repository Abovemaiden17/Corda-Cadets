package com.template.flows.userwithschedulestate

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.Command
import net.corda.core.contracts.StateRef
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.SchedulableFlow
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder

@InitiatingFlow
@SchedulableFlow
class VerifyRegisterFlow(private val stateRef: StateRef) : FlowLogic<SignedTransaction>()
{
    @Suspendable
        override fun call(): SignedTransaction {
        val trans = transaction(input())
        val signedTransaction = verifyAndSign(trans)
        return subFlow(FinalityFlow(signedTransaction, listOf()))
    }
    private fun input():RegisterState
    {
        val inputstate = serviceHub.toStateAndRef<RegisterState>(stateRef).state.data
        return RegisterState(initiator = inputstate.initiator,verify = true,requestTime = inputstate.requestTime,delay = 20)
    }
    private fun transaction(state: RegisterState)
            = TransactionBuilder(notary = serviceHub.networkMapCache.notaryIdentities.first()).apply {
        val input = serviceHub.toStateAndRef<RegisterState>(stateRef)
        val command = Command(RegisterContract.Commands.User(),ourIdentity.owningKey)
        addInputState(input)
        addCommand(command)
        addOutputState(state = state, contract = RegisterContract.CONTRACT_ID)
    }
    fun verifyAndSign(transaction: TransactionBuilder): SignedTransaction {
        transaction.verify(serviceHub)
        return serviceHub.signInitialTransaction(transaction)
    }
}