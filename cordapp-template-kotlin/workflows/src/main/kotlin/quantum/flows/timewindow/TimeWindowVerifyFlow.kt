package quantum.flows.timewindow

import co.paralleluniverse.fibers.Suspendable
import com.github.benmanes.caffeine.cache.Expiry
import net.corda.core.contracts.TimeWindow
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.seconds
import quantum.contracts.TimeWindowContract
import quantum.functions.timewindow.TimeWindowFunctions
import quantum.states.TimeWindowState
import java.time.Duration

@InitiatingFlow
@StartableByRPC
class TimeWindowVerifyFlow(private val status: Boolean,
                  private val counterParty: String,
                  private val linearId: UniqueIdentifier): TimeWindowFunctions()
{
    @Suspendable
    override fun call(): SignedTransaction {
        val issuance = issue()
        val sessions = initiateFlow(stringToParty(counterParty))
        val signedTransaction = verifyAndSign(issuance)
        val transactionSignedByParties = collectSignature(transaction = signedTransaction, sessions = listOf(sessions))
        return recordTransactionWithOtherParty(transaction = transactionSignedByParties, sessions = listOf(sessions))
    }

    private fun outState(): TimeWindowState
    {
        return TimeWindowState(
                status = status,
                party = ourIdentity,
                counter = stringToParty(counterParty),
                linearId = linearId
        )
    }

    private fun issue(): TransactionBuilder =
            TransactionBuilder(notary = inputStateRef(linearId).state.notary).apply {
                val registerTime = getTime(linearId)
                val timeWindow = TimeWindow.fromOnly(registerTime.plusSeconds(30))
                addInputState(inputStateRef(linearId))
                addOutputState(outState(), TimeWindowContract.TIMEWINDOW_ID)
                addCommand(TimeWindowContract.Commands.Verify(), listOf(ourIdentity.owningKey, stringToParty(counterParty).owningKey))
                setTimeWindow(timeWindow)
            }
}

@InitiatedBy(TimeWindowVerifyFlow::class)
class TimeWindowVerifyFlowResponder(val flowSession: FlowSession) : FlowLogic<SignedTransaction>()
{
    @Suspendable
    override fun call(): SignedTransaction {
        subFlow(object : SignTransactionFlow(flowSession)
        {
            override fun checkTransaction(stx: SignedTransaction) = requireThat {
                val output = stx.tx.outputs.single().data
                "This must be a verify transaction" using (output is TimeWindowState)
            }
        })
        return subFlow(ReceiveFinalityFlow(otherSideSession = flowSession))
    }
}
