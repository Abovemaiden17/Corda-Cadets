package com.template.flows.DVP

import co.paralleluniverse.fibers.Suspendable
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.r3.corda.lib.tokens.contracts.utilities.amount
import com.template.DVPstateAndContract.Amount
import com.template.DVPstateAndContract.trycontract
import com.template.DVPstateAndContract.trystate
import net.corda.core.contracts.Command
import net.corda.core.contracts.TransactionState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.*
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClientBuilder
import java.io.BufferedReader
import java.io.InputStreamReader

@StartableByRPC
@InitiatingFlow
class tryflow : FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call() : SignedTransaction {

        val userRegister = userRegister(outPUTstate())
        val signedTransaction = verifyAndSign(transaction = userRegister)
        val sessions = emptyList<FlowSession>()
        val transactionSignedByParties = collectSignature(transaction = signedTransaction, sessions = sessions)



        return recordTransaction(transaction = transactionSignedByParties, sessions = sessions)
    }

    @Suspendable
    private fun collectSignature(
            transaction: SignedTransaction,
            sessions: List<FlowSession>
    ): SignedTransaction = subFlow(CollectSignaturesFlow(transaction, sessions))

    private fun userRegister(state: trystate): TransactionBuilder{
        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val cmd = Command(trycontract.Commands.Beat(),ourIdentity.owningKey)
        val txBuilder = TransactionBuilder(notary)
                .addOutputState(state, trycontract.contractID)
                .addCommand(cmd)
        return txBuilder
    }



    private fun outPUTstate(): trystate {

        val httpclient = HttpClientBuilder.create().build()
        val request = HttpGet("https://api.exchangeratesapi.io/latest?base=USD&symbols=PHP,USD")
        val response = httpclient.execute(request)
        val inputStreamReader = InputStreamReader(response.entity.content)

        val bufferReader = BufferedReader(inputStreamReader).use {
            val stringBuff = StringBuffer()
            var inputLine = it.readLine()
            while (inputLine != null) {
                stringBuff.append(inputLine)
                inputLine = it.readLine()
            }
            val gson = GsonBuilder().create()
            val jsonWholeObject = gson.fromJson(stringBuff.toString(), JsonObject::class.java)

            val basee = jsonWholeObject.get("base")
//            val ratess = jsonWholeObject.get("rates")
            val dates = jsonWholeObject.get("date")
            println("$basee")
            println("$dates")


            return trystate(
                    rates = Amount("USD",0,"PHP",0),
                    base = basee.toString(),
                    date = dates.toString(),
                    linearId = UniqueIdentifier(),
                    participants = listOf(ourIdentity))
        }


    }

    private fun verifyAndSign(transaction: TransactionBuilder): SignedTransaction {
        transaction.verify(serviceHub)

        return serviceHub.signInitialTransaction(transaction)
    }

    private fun recordTransaction(transaction: SignedTransaction, sessions: List<FlowSession>): SignedTransaction =
            subFlow(FinalityFlow(transaction, sessions))

}



