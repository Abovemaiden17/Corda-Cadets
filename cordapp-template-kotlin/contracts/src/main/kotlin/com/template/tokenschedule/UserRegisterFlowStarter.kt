package com.template.tokenschedule

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.Command
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import java.time.Instant


@InitiatingFlow
@StartableByRPC
class UserRegisterFlowStarter : FlowFunction() {

    @Suspendable
    override fun call(): SignedTransaction {
        val signedTx = verifyAndSign(transaction())
        return recordTransaction(signedTx, listOf())
    }

    private fun outputState(): UserRegisterState {
        return UserRegisterState(
                initiator = ourIdentity,
                approved = false,
                activityTimeNext = Instant.now().plusSeconds(1),
                linearId = UniqueIdentifier()
        )
    }

    private fun transaction() = TransactionBuilder(notary = serviceHub.networkMapCache.notaryIdentities.first()).apply {
        val output = outputState()
        val cmd = Command(UserRegisterContract.Commands.Register(), ourIdentity.owningKey)
        addOutputState(output, UserRegisterContract.contractID)
        addCommand(cmd)

    }


}
