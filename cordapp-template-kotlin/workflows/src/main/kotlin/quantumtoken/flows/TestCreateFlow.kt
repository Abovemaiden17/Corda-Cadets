package quantumtoken.flows

import com.r3.corda.lib.tokens.money.FiatCurrency
import com.r3.corda.lib.tokens.workflows.flows.rpc.CreateEvolvableTokens
import net.corda.core.contracts.Amount
import net.corda.core.contracts.TransactionState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.StartableByRPC
import net.corda.core.transactions.SignedTransaction
import quantumtoken.contracts.TestContract
import quantumtoken.functions.TestFunctions
import quantumtoken.states.TestState

@StartableByRPC
class TestCreateFlow (private val currency: String,
                     private val amount: Long): TestFunctions()
{
    override fun call(): SignedTransaction
    {

        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val state = TransactionState(outState(), TestContract.TEST_ID, notary)
        val transaction = subFlow(CreateEvolvableTokens(state))
        return recordTransactionWithoutOtherParty(transaction)
    }

    private fun outState(): TestState
    {
        return TestState(
                "Quantum Crowd",
                valuation = Amount(amount, FiatCurrency.getInstance(currency)),
                maintainers = listOf(ourIdentity),
                linearId = UniqueIdentifier()
        )
    }
}