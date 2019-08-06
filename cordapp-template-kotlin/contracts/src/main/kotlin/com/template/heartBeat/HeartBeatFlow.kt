package com.template.heartBeat

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.Command
import net.corda.core.contracts.StateRef
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.SchedulableFlow
import net.corda.core.transactions.SignedTransaction

import net.corda.core.transactions.TransactionBuilder
import java.time.Instant


/**
 * This is the flow that a Heartbeat state runs when it consumes itself to create a new Heartbeat
 * state on the ledger.
 *
 * @param stateRef the existing Heartbeat state to be updated.
 */
@InitiatingFlow
@SchedulableFlow
class HeartbeatFlow(private val stateRef: StateRef) : FlowLogic<SignedTransaction>() {

    @Suspendable
    override fun call(): SignedTransaction {
        val signedTransaction = verifyAndSign (transactionBuilder())
        return recordTransaction (signedTransaction)
    }

    private fun transactionBuilder(): TransactionBuilder{
        val input = serviceHub.toStateAndRef<HeartBeatState>(stateRef)
        val outputState = HeartBeatState(me = ourIdentity, nextActivityTime = Instant.now().plusSeconds(5))
        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val beatCmd = Command(HeartBeatContract.Commands.Beat(), ourIdentity.owningKey)
        return TransactionBuilder(notary)
                .addInputState(input)
                .addOutputState(outputState, HeartBeatContract.contractID)
                .addCommand(beatCmd)
    }

    private fun verifyAndSign (transactionBuilder: TransactionBuilder): SignedTransaction{
        transactionBuilder.verify(serviceHub)
        return serviceHub.signInitialTransaction(transactionBuilder)
    }

    @Suspendable

    private fun recordTransaction(transaction: SignedTransaction): SignedTransaction =
            subFlow(FinalityFlow(transaction, emptyList()))
}

