package com.template.flows.corexchange

import co.paralleluniverse.fibers.Suspendable
import com.r3.corda.lib.tokens.contracts.types.TokenType
import com.template.states.UserState
import com.template.types.UserContract
import net.corda.core.contracts.Amount
import net.corda.core.contracts.Command
import net.corda.core.flows.*
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder

@InitiatingFlow
@StartableByRPC
class RegisterUserFlow(val name: String, val wallet: List<Amount<TokenType>>): FlowLogic<SignedTransaction>()
{
    @Suspendable
    override fun call(): SignedTransaction {
        val transaction = transaction(register())
        val verify = verifyAndSign(transaction)
        val session :List<FlowSession> = emptyList()
        val transactionSignedbyBoth :SignedTransaction = verify
        return recordRegister(transactionSignedbyBoth,session)
    }

    private fun transaction(state: UserState): TransactionBuilder
    {
        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val command = Command(UserContract.Commands.Register(),ourIdentity.owningKey)
        val builder = TransactionBuilder(notary = notary)
                .addCommand(command)
                .addOutputState(state = state,contract = UserContract.ID_Contracts)
        return builder
    }
    private fun register(): UserState
    {
        return UserState(name,wallet, listOf(ourIdentity))
    }
    private fun verifyAndSign(transactionBuilder: TransactionBuilder):SignedTransaction
    {
        transactionBuilder.verify(serviceHub)
        return serviceHub.signInitialTransaction(transactionBuilder)
    }
    @Suspendable
    private fun recordRegister(transaction:SignedTransaction,session: List<FlowSession>): SignedTransaction
    = subFlow(FinalityFlow(transaction,session))

}