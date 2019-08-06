package com.template.increment

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.Amount
import net.corda.core.contracts.Command
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import java.time.Instant

@InitiatingFlow
@StartableByRPC
class StartIncrementFlow (val amount: Long) : FlowLogic<SignedTransaction>() {


    @Suspendable
    override fun call():SignedTransaction {

        fun inputStateRef(): StateAndRef<IncrementState> {


            return serviceHub.vaultService.queryBy<IncrementState>().states.first()
        }
        val inputs = inputStateRef().state.data
        val outputState = IncrementState(inputs.name,inputs.balance.plus(amount),owner = ourIdentity, scheduledTime = Instant.now().plusSeconds(5))
        val cmd = Command(IncrementContract.Commands.Increment(), ourIdentity.owningKey)
        val txBuilder = TransactionBuilder(serviceHub.networkMapCache.notaryIdentities.first())
                .addOutputState(outputState, IncrementContract.contractID)
                .addCommand(cmd)
                .addInputState(inputStateRef())


        val signedTx = serviceHub.signInitialTransaction(txBuilder)


        return subFlow(FinalityFlow(signedTx, emptyList()))
    }
}