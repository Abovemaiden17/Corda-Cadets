package quantumtoken.flows

import co.paralleluniverse.fibers.Suspendable
import com.r3.corda.lib.tokens.workflows.flows.rpc.RedeemNonFungibleTokens
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.transactions.SignedTransaction
import quantumtoken.functions.TestFunctions
import quantumtoken.states.TestState

@InitiatingFlow
@StartableByRPC
class TestRedeemFlow (private val issuer: String,
                      private val evolvableTokenId: String): TestFunctions()
{
    @Suspendable
    override fun call(): SignedTransaction {
        val evolvableTokenType = inputStateRefUsingUUID(stringToUUID(evolvableTokenId)).state.data
        val tokenPtr = evolvableTokenType.toPointer<TestState>()
        val transaction = subFlow(RedeemNonFungibleTokens(tokenPtr, stringToParty(issuer)))
        val sessions = initiateFlow(stringToParty(issuer))
        return recordTransactionWithOtherParty(transaction = transaction, sessions = listOf(sessions))
    }
}

@InitiatedBy(TestRedeemFlow::class)
class TestRedeemFlowResponder(private val flowSession: FlowSession): FlowLogic<SignedTransaction>()
{
    @Suspendable
    override fun call(): SignedTransaction {
        subFlow(object : SignTransactionFlow(flowSession)
        {
            override fun checkTransaction(stx: SignedTransaction) = requireThat {
                val output = stx.tx.outputs.single().data
                "This must be a redeem transaction" using (output is TestState)
            }
        })
        return subFlow(ReceiveFinalityFlow(otherSideSession = flowSession))
    }
}