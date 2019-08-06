package com.template.flows.simpleFlow

import co.paralleluniverse.fibers.Suspendable
import com.template.simpleState.SimpleContract
import com.template.simpleState.SimpleState
import net.corda.core.contracts.Command
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder

@InitiatingFlow
@StartableByRPC

class SimpleRegisterFlow (val name: String,val age: Int):FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call(): SignedTransaction {
        val signedTransaction = verifyAndSign(transactionBuilder())
        return recordTransaction(signedTransaction)
    }

    private fun outputState(): SimpleState{
        return SimpleState(name,age,ourIdentity,ourIdentity)
    }

    private fun transactionBuilder(): TransactionBuilder{
        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val cmd = Command(SimpleContract.Commands.Register(), ourIdentity.owningKey)
        return TransactionBuilder(notary)
                .addOutputState(outputState(),SimpleContract.ID)
                .addCommand(cmd)
    }

    private fun verifyAndSign(transactionBuilder: TransactionBuilder):SignedTransaction{
        transactionBuilder.verify(serviceHub)
        return serviceHub.signInitialTransaction(transactionBuilder)
    }

    @Suspendable
    private fun recordTransaction(transaction: SignedTransaction):SignedTransaction=subFlow(FinalityFlow(transaction, emptyList()))
}