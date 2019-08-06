package com.template.tokenschedule

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.Command
import net.corda.core.contracts.StateRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.SchedulableFlow
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import java.time.Instant

@InitiatingFlow
@SchedulableFlow
class UserRegisterVerifyFlow(private val stateRef: StateRef) : FlowLogic<SignedTransaction>() {

    @Suspendable
    override fun call(): SignedTransaction {
        val signedTx = serviceHub.signInitialTransaction(transaction())
        return subFlow(FinalityFlow(signedTx, listOf()))

    }

    private fun outputState(): UserRegisterState {
        return UserRegisterState(initiator = ourIdentity,
                approved = true,
                activityTimeNext = Instant.now().plusSeconds(1),
                linearId = UniqueIdentifier()

        )

    }

    private fun transaction() = TransactionBuilder(notary = serviceHub.networkMapCache.notaryIdentities.first()).apply {
        val input = serviceHub.toStateAndRef<UserRegisterState>(stateRef)
        val output = outputState()
        val beatCmd = Command(UserRegisterContract.Commands.Register(), ourIdentity.owningKey)
        addInputState(input)
        addOutputState(output, UserRegisterContract.contractID)
        addCommand(beatCmd)
    }
}