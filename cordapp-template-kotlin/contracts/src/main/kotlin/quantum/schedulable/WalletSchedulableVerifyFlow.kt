package quantum.schedulable

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.*
import net.corda.core.contracts.Command
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import java.time.Duration
import java.time.Instant

@InitiatingFlow
@SchedulableFlow
class WalletSchedulableVerifyFlow (private val stateRef: StateRef): WalletSchedulableFunctions()
{
    @Suspendable
    override fun call(): SignedTransaction
    {
        val selfIssuance = selfIssue()
        val signedTx = verifyAndSign(selfIssuance)
        return recordTransactionWithOtherParty(signedTx, listOf())
    }

    private fun outState(): WalletSchedulableState
    {
        val input = serviceHub.toStateAndRef<WalletSchedulableState>(stateRef).state.data
        return WalletSchedulableState(
                registrant = ourIdentity,
                approved = true,
                requestTime = Instant.now(),
                delay = input.delay,
                linearId = input.linearId
        )
    }

    private fun selfIssue(): TransactionBuilder =
            TransactionBuilder(notary = serviceHub.networkMapCache.notaryIdentities.first()).apply{
                val selfIssueCommand =
                        Command(WalletSchedulableContract.Commands.SelfIssue(),
                                ourIdentity.owningKey)
                val input = serviceHub.toStateAndRef<WalletSchedulableState>(stateRef)
                val registerTime = getTime(input.state.data.linearId)
                addInputState(input)
                addOutputState(outState(), WalletSchedulableContract.WALLETSCHEDULE_ID)
                addCommand(selfIssueCommand)
    }
}