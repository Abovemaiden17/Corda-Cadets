package com.template.heartBeat

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.Command
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import java.time.Instant

/**
 * Creates a Heartbeat state on the ledger.
 *
 * Every Heartbeat state has a scheduled activity to start a flow to consume itself and produce a
 * new Heartbeat state on the ledger after five seconds.
 *
 * By consuming the existing Heartbeat state and creating a new one, a new scheduled activity is
 * created.
 */
@InitiatingFlow
@StartableByRPC
class StartHeartBeatFlow : FlowLogic<SignedTransaction>() {


    @Suspendable
    override fun call():SignedTransaction {

        val outputState = HeartBeatState(me = ourIdentity, nextActivityTime = Instant.now().plusSeconds(5))
        val cmd = Command(HeartBeatContract.Commands.Beat(), ourIdentity.owningKey)
        val txBuilder = TransactionBuilder(serviceHub.networkMapCache.notaryIdentities.first())
                .addOutputState(outputState, HeartBeatContract.contractID)
                .addCommand(cmd)


        val signedTx = serviceHub.signInitialTransaction(txBuilder)


        return subFlow(FinalityFlow(signedTx, emptyList()))
    }
}