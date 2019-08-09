package com.template.flows.corexchange

import co.paralleluniverse.fibers.Suspendable
import com.template.types.UserContract
import net.corda.core.contracts.Command
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder

@InitiatingFlow
@StartableByRPC
class MoveTokenFlow (val senderId: UniqueIdentifier,
                     val receiverId: UniqueIdentifier,
                     val currency: String,
                     val amount: Long): FunctionFlows()
{
    @Suspendable
    override fun call(): SignedTransaction {
        val transaction = transaction()
        val verify = verifyAndSign(transaction)
        val transactionSignedByBothParties: SignedTransaction = verify
        return recordUpdate(transactionSignedByBothParties, listOf())
    }
    private fun transaction(): TransactionBuilder
    {
        val sender = queryState(linearId = senderId).state.data
        val receiver = queryState(linearId = receiverId).state.data
        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val command = Command(UserContract.Commands.Register(),ourIdentity.owningKey)
        val newSenderWallet = updatesenderWallet(senderId,currency,amount)
                .sortedBy{ it.token.tokenIdentifier}.toMutableList()
        val newReceiverWallet = updatereceiverWallet(receiverId,currency,amount)
                .sortedBy { it.token.tokenIdentifier }.toMutableList()
        val builder = TransactionBuilder(notary = notary)
                .addCommand(command)
                .addOutputState(sender.copy(wallet = newSenderWallet),contract = UserContract.ID_Contracts)
                .addOutputState(receiver.copy(wallet = newReceiverWallet),contract = UserContract.ID_Contracts)
                .addInputState(queryState(receiverId))
                .addInputState(queryState(senderId))
        return builder
    }
}