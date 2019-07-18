package quantum.schedulable

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.*
import net.corda.core.contracts.Command
import net.corda.core.flows.*
import net.corda.core.transactions.TransactionBuilder

@InitiatingFlow
@SchedulableFlow
class WalletSchedulableSelfIssueFlow (private val stateRef: StateRef): FlowLogic<Long>()
{
    @Suspendable
    override fun call(): Long
    {
        val selfIssuance = selfIssue()
        val signedTransaction = serviceHub.signInitialTransaction(selfIssuance)
        val sessions = emptyList<FlowSession>()
        subFlow(FinalityFlow(signedTransaction, sessions))
        val input = serviceHub.toStateAndRef<WalletSchedulableState>(stateRef)
        return input.state.data.amount
    }

    private fun outState(): WalletSchedulableState
    {
        val input = serviceHub.toStateAndRef<WalletSchedulableState>(stateRef)
        return WalletSchedulableState(
                initiator = ourIdentity,
                amount = input.state.data.amount + 10
        )
    }

    private fun selfIssue(): TransactionBuilder =
            TransactionBuilder(notary = serviceHub.networkMapCache.notaryIdentities.first()).apply{
                val selfIssueCommand =
                        Command(WalletSchedulableContract.Commands.SelfIssue(),
                                ourIdentity.owningKey)
                val input = serviceHub.toStateAndRef<WalletSchedulableState>(stateRef)
                addInputState(input)
                addOutputState(outState(), WalletSchedulableContract.WALLETSCHEDULE_ID)
                addCommand(selfIssueCommand)
            }
}