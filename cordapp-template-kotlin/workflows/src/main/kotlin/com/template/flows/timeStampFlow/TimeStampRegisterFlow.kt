package com.template.flows.timeStampFlow

import co.paralleluniverse.fibers.Suspendable
import com.template.timeStamp.TimeStampContract
import com.template.timeStamp.TimeStampState
import net.corda.core.contracts.Command
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder

@InitiatingFlow
@StartableByRPC

class TimeStampRegisterFlow : FlowLogic<SignedTransaction>() {

    @Suspendable
    override fun call(): SignedTransaction {
        val signedTransaction = verifyAndSign(transactionBuilder())
        return recordTransaction(signedTransaction)
    }

    private fun outputState(): TimeStampState {
        return TimeStampState(condition = false, sender = ourIdentity, receiver = ourIdentity)
    }

    private fun transactionBuilder(): TransactionBuilder {
        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val cmd = Command(TimeStampContract.Commands.Register(), ourIdentity.owningKey)
        return TransactionBuilder(notary)
                .addOutputState(outputState(), TimeStampContract.ID)
                .addCommand(cmd)
    }

    private fun verifyAndSign (transactionBuilder: TransactionBuilder) : SignedTransaction {
        transactionBuilder.verify(serviceHub)
        return serviceHub.signInitialTransaction(transactionBuilder)
    }


    @Suspendable fun recordTransaction (
            transaction: SignedTransaction
    ):SignedTransaction = subFlow(FinalityFlow(transaction , emptyList()))

}
