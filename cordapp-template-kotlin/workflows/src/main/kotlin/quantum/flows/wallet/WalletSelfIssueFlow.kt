package quantum.flows.wallet

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.*
import net.corda.core.contracts.Command
import net.corda.core.flows.*
import net.corda.core.flows.ReceiveFinalityFlow
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import quantum.*
import quantum.contracts.WalletContract
import quantum.functions.wallet.WalletFunctions
import quantum.states.WalletState

@InitiatingFlow
@StartableByRPC
class WalletSelfIssueFlow (private val selfIssueAmount: Long,
                         private val linearId: UniqueIdentifier): WalletFunctions()
{
    @Suspendable
    override fun call(): SignedTransaction
    {
        val admin = stringToParty("PartyC")

        progressTracker.currentStep = CREATING
        val selfIssuance = selfIssue(admin)

        progressTracker.currentStep = VERIFYING
        progressTracker.currentStep = SIGNING
        val signedTransaction = verifyAndSign(transaction = selfIssuance)
        val sessions = emptyList<FlowSession>()
        val adminSession = initiateFlow(admin)
        val transactionSignedByParties = collectSignature(transaction = signedTransaction, sessions = sessions)

        progressTracker.currentStep = NOTARIZING
        progressTracker.currentStep = FINALIZING
        return recordTransactionWithOtherParty(transaction = transactionSignedByParties, sessions = listOf(adminSession))
    }

    private fun outState(): WalletState
    {
        val input = inputStateRef(linearId).state.data
        return WalletState(
                wallet = input.wallet.plus(selfIssueAmount),
                amountIssued = input.amountIssued,
                amountPaid = input.amountPaid,
                status = input.status,
                borrower = ourIdentity,
                lender = ourIdentity,
                admin = input.admin,
                linearId = linearId
        )
    }

    private fun selfIssue(PartyC: Party): TransactionBuilder =
            TransactionBuilder(notary = inputStateRef(linearId).state.notary).apply{
                val selfIssueCommand = Command(WalletContract.Commands.SelfIssue(), ourIdentity.owningKey)
                val stateWithAdmin = outState().copy(participants = outState().participants + PartyC)
                addInputState(inputStateRef(linearId))
                addOutputState(stateWithAdmin, WalletContract.WALLET_ID)
                addCommand(selfIssueCommand)
            }
}

@InitiatedBy(WalletSelfIssueFlow::class)
class WalletSelfIssueFlowResponder(val flowSession: FlowSession) : FlowLogic<SignedTransaction>()
{
    @Suspendable
    override fun call(): SignedTransaction
    {
        return subFlow(ReceiveFinalityFlow(otherSideSession = flowSession))
    }
}