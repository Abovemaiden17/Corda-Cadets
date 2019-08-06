package com.template.increment

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.Command
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import java.time.Instant

@InitiatingFlow
@StartableByRPC

class IncrementRegisterFlow :FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call(): SignedTransaction {
        val signedTransaction = verifyAndSign(transactionBuilder())
        return recordTransaction(signedTransaction)
    }
     private fun outputState():IncrementState {
         return IncrementState(name = String(),balance = 0,owner = ourIdentity,scheduledTime = Instant.now().plusSeconds(1))
     }

    private fun transactionBuilder():TransactionBuilder{
        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val incrementCmd = Command(IncrementContract.Commands.Increment(), ourIdentity.owningKey)
        return TransactionBuilder(notary)
                .addOutputState(outputState(),IncrementContract.contractID)
                .addCommand(incrementCmd)
    }

    private fun verifyAndSign(transactionBuilder: TransactionBuilder):SignedTransaction{
        transactionBuilder.verify(serviceHub)
        return serviceHub.signInitialTransaction(transactionBuilder)
    }

    @Suspendable
    private fun recordTransaction (
            transaction: SignedTransaction):SignedTransaction = subFlow(FinalityFlow(transaction, emptyList()))


}