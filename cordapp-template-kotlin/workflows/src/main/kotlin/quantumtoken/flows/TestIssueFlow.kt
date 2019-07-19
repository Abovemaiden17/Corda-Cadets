package quantumtoken.flows

import net.corda.core.transactions.SignedTransaction
import quantumtoken.functions.TestFunctions

class TestIssueFlow (private val evolvableTokenId: String,
                     private val counterParty: String): TestFunctions()
{
    override fun call(): SignedTransaction {
        val uuid = stringToUUID(evolvableTokenId)
        TODO()
    }


}