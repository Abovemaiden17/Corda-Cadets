package com.template.flows.tokenSdkFlow

import co.paralleluniverse.fibers.Suspendable
import com.r3.corda.lib.tokens.contracts.types.TokenType
import com.r3.corda.lib.tokens.money.FiatCurrency
import com.template.tokenSdk.UserContract
import com.template.tokenSdk.UserState
import net.corda.core.contracts.Amount
import net.corda.core.contracts.Command
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.*
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder

@Suppress("UNCHECKED_CAST")
@InitiatingFlow
@StartableByRPC
class MoveTokenFlow(val sender : UniqueIdentifier,
                    val receiver: UniqueIdentifier,
//                    val wallet: MutableList<Amount<TokenType>>
                    val amountA: Long,
                    val currencyA: String,
                    val amountB: Long,
                    val currencyB: String

                    ): FlowLogic<SignedTransaction>()
{
    @Suspendable
    override fun call(): SignedTransaction {

        val verify = verifyAndSign(transaction())
        val session :List<FlowSession> = emptyList()
        val transactionSignedbyBoth :SignedTransaction = verify
        return recordRegister(transactionSignedbyBoth,session)
    }

    private fun inputStateAndRefA (sender: UniqueIdentifier): StateAndRef<UserState> {
        val criteria = QueryCriteria.LinearStateQueryCriteria(linearId =  listOf(sender))
        return serviceHub.vaultService.queryBy<UserState>(criteria).states.single()
    }

    private fun inputStateAndRefB (receiver: UniqueIdentifier): StateAndRef<UserState> {
        val criteria = QueryCriteria.LinearStateQueryCriteria(linearId =  listOf(receiver))
        return serviceHub.vaultService.queryBy<UserState>(criteria).states.single()
    }

    private fun outputStateSender (sender: UniqueIdentifier): UserState{
    val input = inputStateAndRefA(sender).state.data
        val thiswallet = input.wallet.filter { it.token.tokenIdentifier == currencyA}.toMutableList()
        val thisamount = thiswallet.minus(Amount(amountA,FiatCurrency.getInstance(currencyA)))
        val totalamount= thisamount[0].quantity + amountA

        val thiswalletA = input.wallet.filter { it.token.tokenIdentifier == currencyB}.toMutableList()
        val thisamountA = thiswalletA.minus(Amount(amountB,FiatCurrency.getInstance(currencyB)))
        val totalamountA= thisamountA[0].quantity + amountB


//        val totalA = listOf(Amount(totalamountA,FiatCurrency.getInstance(currencyB)))

//        val total = listOf((thisamount+  Amount(totalamount,FiatCurrency.getInstance(currencyA))), (thisamountA+ Amount(totalamountA,FiatCurrency.getInstance(currencyB))) )
//        val all = thisamount + total
//        val x = listOf(thisamount,thisamountA)
//////        val all =thisamount+total
////        val all = x+total
        val total = thisamount+Amount(totalamount,FiatCurrency.getInstance(currencyA))
        val totalA = thisamountA+Amount(totalamountA,FiatCurrency.getInstance(currencyB))
        val x = listOf(total,totalA)

//        val allA =thisamountA+totalA





    return UserState(
            input.name,
            x as MutableList<Amount<TokenType>>,
//          listOf(Amount(amountA-amountA, FiatCurrency.getInstance(currencyA.plus(currencyA))),Amount(amountB-amountB, FiatCurrency.getInstance(currencyB+currencyB))),
//            wallet = input.wallet.plus(wallet) as MutableList<Amount<TokenType>>,
            listOf(ourIdentity),
            linearId = sender

    )
}
    private fun outputStateReceiver (receiver: UniqueIdentifier): UserState{
        val input = inputStateAndRefB(receiver).state.data
//        val thiswalletReceiver = input.wallet.filter { it.token.tokenIdentifier == currencyA}
//        val thisamountReceiver = thiswalletReceiver.plus(Amount(amountA,FiatCurrency.getInstance(currencyA)))
//
//        val thiswalletReceiverA = input.wallet.filter { it.token.tokenIdentifier == currencyB}
//        val thisamountReceiverA = thiswalletReceiverA.plus(Amount(amountA,FiatCurrency.getInstance(currencyB)))




        return UserState(
                input.name,
                (input.wallet + listOf(Amount(amountA, FiatCurrency.getInstance(currencyA)),Amount(amountB, FiatCurrency.getInstance(currencyB)))) as MutableList<Amount<TokenType>>,
//                wallet = input.wallet.plus(wallet) as MutableList<Amount<TokenType>>,
                listOf(ourIdentity),
                linearId = receiver
        )
    }

    private fun transaction(): TransactionBuilder
    {
        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val command = Command(UserContract.Commands.Register(),ourIdentity.owningKey)
        val builder = TransactionBuilder(notary = notary)
                .addCommand(command)
                .addInputState(inputStateAndRefA(sender))
                .addInputState(inputStateAndRefB(receiver))
                .addOutputState(outputStateSender(sender),UserContract.ID_Contracts)
                .addOutputState(outputStateReceiver(receiver),UserContract.ID_Contracts)
        return builder
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