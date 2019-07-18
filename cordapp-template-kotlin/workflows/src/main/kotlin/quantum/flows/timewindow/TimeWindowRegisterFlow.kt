package quantum.flows.timewindow

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.Command
import net.corda.core.flows.FlowSession
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.SchedulableFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import quantum.contracts.TimeWindowContract
import quantum.functions.timewindow.TimeWindowFunctions
import quantum.states.TimeWindowState

@InitiatingFlow
@StartableByRPC
class TimeWindowRegisterFlow : TimeWindowFunctions()
{
    @Suspendable
    override fun call(): SignedTransaction
    {
        val registration = register()
        val signedTransaction = verifyAndSign(transaction = registration)
        val sessions = emptyList<FlowSession>()
        val transactionSignedByParties = collectSignature(transaction = signedTransaction, sessions = sessions)
        return recordTransactionWithOtherParty(transaction = transactionSignedByParties, sessions = sessions)
    }

    private fun outState(): TimeWindowState
    {
        return TimeWindowState(
                status = false,
                party = ourIdentity,
                counter = ourIdentity
        )
    }

    private fun register(): TransactionBuilder =
            TransactionBuilder(notary = serviceHub.networkMapCache.notaryIdentities.first()).apply {
                addOutputState(outState(), TimeWindowContract.TIMEWINDOW_ID)
                addCommand(Command(TimeWindowContract.Commands.Register(), ourIdentity.owningKey))
            }
}