package quantumtoken.flows

import co.paralleluniverse.fibers.Suspendable
import com.r3.corda.lib.tokens.money.FiatCurrency
import com.r3.corda.lib.tokens.workflows.flows.rpc.CreateEvolvableTokens
import net.corda.core.contracts.Amount
import net.corda.core.contracts.TransactionState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.StartableByRPC
import net.corda.core.transactions.SignedTransaction
import quantumtoken.contracts.HouseContract
import quantumtoken.functions.TestFunctions
import quantumtoken.states.HouseState
import java.util.*

@StartableByRPC
class TestCreateHouseFlow (private val owner: String,
                           private val currency: String,
                           private val amount: Long): TestFunctions()
{
    @Suspendable
    override fun call(): SignedTransaction
    {
        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val state = TransactionState(outState(), HouseContract.TEST_ID, notary)
        return subFlow(CreateEvolvableTokens(state))
    }

    private fun outState(): HouseState
    {
        return HouseState(
                owner = stringToParty(owner),
                address = "Quantum Crowd",
                valuation = Amount(amount, FiatCurrency.getInstance(currency)),
                maintainers = listOf(ourIdentity),
                linearId = UniqueIdentifier.fromString(UUID.randomUUID().toString())
        )
    }
}