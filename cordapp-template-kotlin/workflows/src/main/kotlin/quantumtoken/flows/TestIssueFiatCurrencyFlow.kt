package quantumtoken.flows

import co.paralleluniverse.fibers.Suspendable
import com.r3.corda.lib.tokens.contracts.types.IssuedTokenType
import com.r3.corda.lib.tokens.contracts.utilities.heldBy
import com.r3.corda.lib.tokens.contracts.utilities.issuedBy
import com.r3.corda.lib.tokens.money.FiatCurrency
import com.r3.corda.lib.tokens.workflows.flows.rpc.IssueTokens
import net.corda.core.contracts.Amount
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.transactions.SignedTransaction
import quantumtoken.functions.FungibleTokenFunctions
import quantumtoken.functions.TestFunctions
import quantumtoken.states.FungibleTokenState

@InitiatingFlow
@StartableByRPC
class TestIssueFiatCurrencyFlow (private val currency: String,
                                 private val amount: Long,
                                 private val recipient: String): TestFunctions()
{
    @Suspendable
    override fun call(): SignedTransaction
    {
        val tokenIssued = IssuedTokenType(ourIdentity, FiatCurrency.getInstance(currency))
        val fungibleToken = FungibleTokenState(Amount(amount, tokenIssued), stringToParty(recipient))
        return subFlow(IssueTokens(listOf(fungibleToken)))
    }
}