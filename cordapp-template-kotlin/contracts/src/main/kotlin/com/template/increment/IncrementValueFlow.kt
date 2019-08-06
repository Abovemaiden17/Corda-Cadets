package com.template.increment

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.Command
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.StateRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.SchedulableFlow
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
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
class IncrementValueFlow(private val stateRef: StateRef, private val linearId:UniqueIdentifier) : FlowLogic<SignedTransaction>() {

    @Suspendable
    override fun call(): SignedTransaction {
        val signedTransaction = verifyAndSign (transactionBuilder())
        return recordTransaction (signedTransaction)
    }

    private fun inputStateRef(): StateAndRef<IncrementState> {
        val criteria = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(linearId))

        return serviceHub.vaultService.queryBy<IncrementState>(criteria).states.first()
    }

    private fun transactionBuilder(): TransactionBuilder {
        val inputs = inputStateRef().state.data
        val input = serviceHub.toStateAndRef<IncrementState>(stateRef)
        val outputState = IncrementState(inputs.name,inputs.balance,owner = ourIdentity, scheduledTime = Instant.now().plusSeconds(5))
        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val beatCmd = Command(IncrementContract.Commands.Increment(), ourIdentity.owningKey)
        return TransactionBuilder(notary)
                .addInputState(input)
                .addOutputState(outputState, IncrementContract.contractID)
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

