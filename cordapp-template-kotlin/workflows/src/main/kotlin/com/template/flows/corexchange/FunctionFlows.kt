package com.template.flows.corexchange

import co.paralleluniverse.fibers.Suspendable
import com.r3.corda.lib.tokens.contracts.types.TokenType
import com.r3.corda.lib.tokens.money.FiatCurrency
import com.template.states.UserState
import net.corda.core.contracts.Amount
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import java.lang.IllegalArgumentException

abstract class FunctionFlows : FlowLogic<SignedTransaction>()
{
    fun queryState(linearId: UniqueIdentifier): StateAndRef<UserState> {
        val criteria = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(linearId))
        return serviceHub.vaultService.queryBy<UserState>(criteria).states.single()
    }
    @Suspendable
    fun recordRegister(transaction:SignedTransaction,session: List<FlowSession>): SignedTransaction
            = subFlow(FinalityFlow(transaction,session))

    fun updatesenderWallet(linearId: UniqueIdentifier,currency:String,amount: Long):  MutableList<Amount<TokenType>>
    {
        val state = queryState(linearId).state.data
        val stateWallet = state.wallet.find { state -> state.token.tokenIdentifier == currency }
               ?: throw IllegalArgumentException("No currency found")
        val wallet = state.wallet.filter { stateWallet.token.tokenIdentifier == currency }
        val totalamount = stateWallet.quantity - amount
        val token = Amount(totalamount,FiatCurrency.getInstance(stateWallet.token.tokenIdentifier))
        val minuswallet = wallet.minus(stateWallet).toMutableList()
        return minuswallet.plus(token).toMutableList()
    }
    fun updatereceiverWallet(linearId: UniqueIdentifier,currency:String,amount: Long): MutableList<Amount<TokenType>>
    {
        val state = queryState(linearId).state.data
        val stateWallet = state.wallet.find { state -> state.token.tokenIdentifier == currency }
                ?: throw IllegalArgumentException("No currency found")
        val wallet = state.wallet.filter { stateWallet.token.tokenIdentifier == currency }
        val totalamount = stateWallet.quantity + amount
        val token = Amount(totalamount,FiatCurrency.getInstance(stateWallet.token.tokenIdentifier))
        val minuswallet = wallet.minus(stateWallet)
        return minuswallet.plus(token).toMutableList()
    }
    fun verifyAndSign(transaction: TransactionBuilder): SignedTransaction
    {
        transaction.verify(serviceHub)
        return serviceHub.signInitialTransaction(transaction)
    }
    @Suspendable
    fun recordUpdate(transaction: SignedTransaction,session: List<FlowSession>): SignedTransaction
            = subFlow(FinalityFlow(transaction = transaction,sessions =  session))
}