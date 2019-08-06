package com.template.flows.simpleFlow

import co.paralleluniverse.fibers.Suspendable
import com.template.simpleState.SimpleContract
import com.template.simpleState.SimpleState
import net.corda.core.contracts.Command
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder

@InitiatingFlow
@StartableByRPC

class SimpleVerifyFlowFlow (private val counterParty: Party, private val linearId: UniqueIdentifier): FlowLogic<SignedTransaction>() {

    @Suspendable
    override fun call(): SignedTransaction {
        val sessions = initiateFlow(counterParty)
        val signedTransaction = verifyAndSign(transactionBuilder())
        val transactionSignedByAllParties = collectSignature(signedTransaction, listOf(sessions))
        return recordTransaction(transactionSignedByAllParties, listOf(sessions))
    }

    private fun inputStateRef():StateAndRef<SimpleState> {
        val criteria = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(linearId))
        return serviceHub.vaultService.queryBy<SimpleState>(criteria).states.single()
    }

    private fun outputState(): SimpleState {
        val input = inputStateRef().state.data
        return SimpleState(input.name,input.age,ourIdentity,counterParty,input.linearId)
    }

    private fun transactionBuilder(): TransactionBuilder {
        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val cmd = Command(SimpleContract.Commands.Register(), listOf(ourIdentity.owningKey,counterParty.owningKey))
        return TransactionBuilder(notary)
                .addOutputState(outputState(), SimpleContract.ID)
                .addCommand(cmd)
    }

    private fun verifyAndSign(transactionBuilder: TransactionBuilder): SignedTransaction {
        transactionBuilder.verify(serviceHub)
        return serviceHub.signInitialTransaction(transactionBuilder)
    }

    @Suspendable
    private fun collectSignature(
            transaction: SignedTransaction,
            sessions: List<FlowSession>
    ): SignedTransaction = subFlow(CollectSignaturesFlow(transaction, sessions))


    @Suspendable
    private fun recordTransaction(
            transaction: SignedTransaction,
            sessions: List<FlowSession>
    ): SignedTransaction =subFlow(FinalityFlow(transaction, sessions))
}

@InitiatedBy(SimpleVerifyFlowFlow::class)
class SimpleVerifyFlowResponder(val flowSession: FlowSession): FlowLogic<SignedTransaction>() {

    @Suspendable
    override fun call(): SignedTransaction {
        val signedTransactionFlow = object : SignTransactionFlow(flowSession) {
            override fun checkTransaction(stx: SignedTransaction) = requireThat {
                val output = stx.tx.outputs.single().data
                "This must be an IOU transaction" using (output is SimpleState)
            }
        }

        val txWeJustSignedId = subFlow(signedTransactionFlow)

        return subFlow(ReceiveFinalityFlow(otherSideSession = flowSession, expectedTxId = txWeJustSignedId.id))
    }
}