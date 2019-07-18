package quantum.schedulable

import net.corda.core.contracts.Command
import net.corda.core.flows.*
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder

@InitiatingFlow
@StartableByRPC
class WalletSchedulableRegisterFlow : FlowLogic<Unit>()
{
    override fun call()
    {
        val registration = register()
        val signedTx = serviceHub.signInitialTransaction(registration)
        val sessions = emptyList<FlowSession>()
        subFlow(FinalityFlow(signedTx, sessions))
    }

    private fun outState() : WalletSchedulableState
    {
        return WalletSchedulableState(
                initiator = ourIdentity,
                amount = 0
        )
    }

    private fun register() : TransactionBuilder =
            TransactionBuilder(notary = serviceHub.networkMapCache.notaryIdentities.first()).apply{
                addOutputState(outState(), WalletSchedulableContract.WALLETSCHEDULE_ID)
                addCommand(Command(WalletSchedulableContract.Commands.Register(), ourIdentity.owningKey))
            }
}

