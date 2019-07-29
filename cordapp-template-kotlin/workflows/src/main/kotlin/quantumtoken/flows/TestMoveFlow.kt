package quantumtoken.flows

import co.paralleluniverse.fibers.Suspendable
import com.r3.corda.lib.tokens.workflows.flows.rpc.CreateEvolvableTokens
import com.r3.corda.lib.tokens.workflows.flows.rpc.MoveNonFungibleTokens
import com.r3.corda.lib.tokens.workflows.types.PartyAndToken
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import quantumtoken.functions.TestFunctions
import quantumtoken.states.TestState

@InitiatingFlow
@StartableByRPC
class TestMoveFlow (private val counterParty: String,
                    private val evolvableTokenId: String): TestFunctions()
{
    @Suspendable
    override fun call(): SignedTransaction {
        val evolvableTokenType = inputStateRefUsingUUID(stringToUUID(evolvableTokenId)).state.data
        val tokenPtr = evolvableTokenType.toPointer<TestState>()
        val partyAndToken = getPartyAndToken(stringToParty(counterParty), tokenPtr)
        val transaction = subFlow(MoveNonFungibleTokens(partyAndToken))
        val sessions = initiateFlow(stringToParty(counterParty))
        return recordTransactionWithOtherParty(transaction = transaction, sessions = listOf(sessions))
    }
}

@InitiatedBy(TestMoveFlow::class)
class TestMoveFlowResponder(private val flowSession: FlowSession): FlowLogic<SignedTransaction>()
{
    @Suspendable
    override fun call(): SignedTransaction
    {
        subFlow(object : SignTransactionFlow(flowSession)
        {
            override fun checkTransaction(stx: SignedTransaction) = requireThat {
                val output = stx.tx.outputs.single().data
                "This must be a move transaction" using (output is TestState)
            }
        })
        return subFlow(ReceiveFinalityFlow(otherSideSession = flowSession))
    }
}
