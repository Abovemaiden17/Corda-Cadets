package quantumtoken.flows

import co.paralleluniverse.fibers.Suspendable
import com.r3.corda.lib.tokens.contracts.states.FungibleToken
import com.r3.corda.lib.tokens.contracts.utilities.heldBy
import com.r3.corda.lib.tokens.contracts.utilities.issuedBy
import com.r3.corda.lib.tokens.money.FiatCurrency
import com.r3.corda.lib.tokens.workflows.flows.rpc.IssueTokens
import jdk.nashorn.internal.parser.TokenType
import net.corda.core.contracts.Amount
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.transactions.SignedTransaction
import org.bouncycastle.cms.bc.BcKEKEnvelopedRecipient
import quantumtoken.functions.TestFunctions

@InitiatingFlow
@StartableByRPC
class TestFiatCurrencyIssueFlow (private val currency: String,
                                 private val amount: Long,
                                 private val recipient: String): TestFunctions()
{
    @Suspendable
    override fun call(): SignedTransaction
    {
        val token = FiatCurrency.getInstance(currency)
        val fungibleToken = Amount(amount, token) issuedBy ourIdentity heldBy stringToParty(recipient)
        return subFlow(IssueTokens(listOf(fungibleToken)))
    }
}