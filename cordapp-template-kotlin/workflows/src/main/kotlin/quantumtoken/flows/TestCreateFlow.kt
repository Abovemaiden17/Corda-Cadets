package quantumtoken.flows

import co.paralleluniverse.fibers.Suspendable
import com.r3.corda.lib.tokens.contracts.states.NonFungibleToken
import com.r3.corda.lib.tokens.contracts.types.IssuedTokenType
import com.r3.corda.lib.tokens.contracts.utilities.getAttachmentIdForGenericParam
import com.r3.corda.lib.tokens.money.FiatCurrency
import com.r3.corda.lib.tokens.workflows.flows.rpc.CreateEvolvableTokens
import com.r3.corda.lib.tokens.workflows.flows.rpc.IssueTokens
import net.corda.core.contracts.Amount
import net.corda.core.contracts.TransactionState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.StartableByRPC
import net.corda.core.transactions.SignedTransaction
import quantumtoken.contracts.TestContract
import quantumtoken.functions.TestFunctions
import quantumtoken.states.TestState
import java.util.*

@StartableByRPC
class TestCreateFlow (private val owner: String,
                      private val currency: String,
                      private val amount: Long): TestFunctions()
{
    @Suspendable
    override fun call(): SignedTransaction
    {
        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val state = TransactionState(outState(), TestContract.TEST_ID, notary)
        return subFlow(CreateEvolvableTokens(state))
    }

    private fun outState(): TestState
    {
        return TestState(
                owner = stringToParty(owner),
                address = "Quantum Crowd",
                valuation = Amount(amount, FiatCurrency.getInstance(currency)),
                maintainers = listOf(ourIdentity),
                linearId = UniqueIdentifier.fromString(UUID.randomUUID().toString())
        )
    }
}