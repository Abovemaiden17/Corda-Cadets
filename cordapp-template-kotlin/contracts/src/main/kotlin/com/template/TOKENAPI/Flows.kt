package com.template.TOKENAPI

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.Command
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker


const val TABLE_NAME = "crypto_values"

@InitiatingFlow
@StartableByRPC
class UserFlow : FlowFunctions() {

    @Suspendable
    override fun call(): SignedTransaction {
        progressTracker.currentStep = GENERATING_TRANSACTION
        val userRegister = userRegister()
        progressTracker.currentStep = VERIFYING_TRANSACTION
        progressTracker.currentStep = SIGNING_TRANSACTION
        val signedTransaction = verifyAndSign(transaction = userRegister)
        val sessions = emptyList<FlowSession>() // empty because the owner's signature is just needed
        val transactionSignedByParties = collectSignature(transaction = signedTransaction, sessions = sessions)

        progressTracker.currentStep = FINALISING_TRANSACTION
        return recordTransactionWithoutOtherParty(transaction = transactionSignedByParties)
    }
    private fun outputState(): tokenAPIstate {
        return tokenAPIstate("", 0, ourIdentity, UniqueIdentifier())

    }
    private fun userRegister(): TransactionBuilder {
        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val cmd = Command(tokenAPIcontract.Commands.Beat(), ourIdentity.owningKey)
        val txBuilder = TransactionBuilder(notary)
                .addOutputState(outputState(), tokenAPIcontract.contractID)
                .addCommand(cmd)
        return txBuilder
    }
}

@InitiatedBy(UserFlow::class)
class UserFlowResponder(val flowSession: FlowSession): FlowLogic<SignedTransaction>() {

    @Suspendable
    override fun call(): SignedTransaction {
        val signedTransactionFlow = object : SignTransactionFlow(flowSession) {
            override fun checkTransaction(stx: SignedTransaction) = requireThat {
                val output = stx.tx.outputs.single().data
                "This must be an IOU transaction" using (output is tokenAPIstate)
            }
        }

        val txWeJustSignedId = subFlow(signedTransactionFlow)

        return subFlow(ReceiveFinalityFlow(otherSideSession = flowSession, expectedTxId = txWeJustSignedId.id))
    }
}



@InitiatingFlow
@StartableByRPC
class AddTokenValueFlow(private val token: String, private val value: Int, private val linearId: String) : FlowLogic<Unit>() {
    override val progressTracker: ProgressTracker = ProgressTracker()

    @Suspendable
    override fun call() {
        val databaseService = serviceHub.cordaService(CryptoValuesDatabaseService::class.java)
        databaseService.addTokenValue(token, value, linearId)
    }

    private fun outputState(): tokenAPIstate {

        return tokenAPIstate(token, value, ourIdentity)
    }

}


@InitiatingFlow
@StartableByRPC
class UpdateTokenValueFlow(private val token: String, private val value: Int, private val linearId: String) : FlowLogic<Unit>() {
    override val progressTracker: ProgressTracker = ProgressTracker()

    @Suspendable
    override fun call() {
        val databaseService = serviceHub.cordaService(CryptoValuesDatabaseService::class.java)
        databaseService.updateTokenValue(token, value, linearId)
    }
}


@InitiatingFlow
@StartableByRPC
class QueryTokenValueFlow(private val token: String) : FlowLogic<Int>() {
    override val progressTracker: ProgressTracker = ProgressTracker()

    @Suspendable
    override fun call(): Int {
        val databaseService = serviceHub.cordaService(CryptoValuesDatabaseService::class.java)
        return databaseService.queryTokenValue(token)
    }
}