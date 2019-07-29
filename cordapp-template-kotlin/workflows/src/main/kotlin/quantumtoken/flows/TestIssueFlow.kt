package quantumtoken.flows

import co.paralleluniverse.fibers.Suspendable
import com.r3.corda.lib.tokens.contracts.states.NonFungibleToken
import com.r3.corda.lib.tokens.contracts.types.IssuedTokenType
import com.r3.corda.lib.tokens.contracts.utilities.getAttachmentIdForGenericParam
import com.r3.corda.lib.tokens.workflows.flows.rpc.IssueTokens
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.transactions.SignedTransaction
import quantumtoken.functions.TestFunctions
import quantumtoken.states.TestState
import java.util.*

@InitiatingFlow
@StartableByRPC
class TestIssueFlow (private val evolvableTokenId: String): TestFunctions()
{
    @Suspendable
    override fun call(): SignedTransaction
    {
        val evolvableTokenType = inputStateRefUsingUUID(stringToUUID(evolvableTokenId)).state.data
        val housePtr = evolvableTokenType.toPointer<TestState>()
        val issuedHouseToken = IssuedTokenType(ourIdentity, housePtr)
        val houseToken = NonFungibleToken(issuedHouseToken, evolvableTokenType.owner, UniqueIdentifier.fromString(UUID.randomUUID().toString()), housePtr.getAttachmentIdForGenericParam())
        return subFlow(IssueTokens(listOf(houseToken)))
    }
}