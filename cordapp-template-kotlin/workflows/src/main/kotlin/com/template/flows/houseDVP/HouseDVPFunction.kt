package com.template.flows.houseDVP

import co.paralleluniverse.fibers.Suspendable
import com.r3.corda.lib.tokens.contracts.states.FungibleToken
import com.r3.corda.lib.tokens.contracts.types.IssuedTokenType
import com.r3.corda.lib.tokens.contracts.types.TokenPointer
import com.r3.corda.lib.tokens.contracts.types.TokenType
import com.r3.corda.lib.tokens.contracts.utilities.amount
import com.r3.corda.lib.tokens.money.FiatCurrency
import com.r3.corda.lib.tokens.money.USD
import com.r3.corda.lib.tokens.workflows.types.PartyAndToken
import com.r3.corda.lib.tokens.workflows.utilities.tokenAmountCriteria
import com.template.houseDVP.HouseDVPState
import net.corda.core.contracts.Amount
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.CollectSignaturesFlow
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.FlowSession
import net.corda.core.identity.Party
import net.corda.core.node.services.Vault
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import java.math.BigDecimal
import java.security.PublicKey
import java.util.*

abstract class HouseDVPFunction : FlowLogic<SignedTransaction>()
{
    fun verifyAndSign(transaction: TransactionBuilder, signingPubKeys: List<PublicKey>): SignedTransaction {

        transaction.verify(serviceHub)
        return serviceHub.signInitialTransaction(transaction, signingPubKeys)
    }
    fun inputStateRef(linearId: UniqueIdentifier) : StateAndRef<HouseDVPState> {
        val ref = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(linearId))
        return serviceHub.vaultService.queryBy<HouseDVPState>(ref).states.single()
    }

    fun inputStateRefFiat(linearId: UniqueIdentifier) : StateAndRef<FungibleToken> {
        val ref = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(linearId))
        return serviceHub.vaultService.queryBy<FungibleToken>(ref).states.single()
    }

    @Suspendable
    fun collectSignature(
            transaction: SignedTransaction,
            sessions: List<FlowSession>,
            signingPubKeys: List<PublicKey>
    ): SignedTransaction = subFlow(CollectSignaturesFlow(transaction, sessions, signingPubKeys))

    @Suspendable
    fun recordTransactionWithOtherParty(transaction: SignedTransaction, sessions: List<FlowSession>) : SignedTransaction {
        return subFlow(FinalityFlow(transaction, sessions))
    }

    @Suspendable
    fun recordTransactionWithoutOtherParty(transaction: SignedTransaction) : SignedTransaction {
        return subFlow(FinalityFlow(transaction, emptyList()))
    }

    fun stringToParty(name: String): Party {
        return serviceHub.identityService.partiesFromName(name, false).singleOrNull()
                ?: throw IllegalArgumentException("No match found for $name")
    }

    fun inputStateRefUsingLinearID(id: UniqueIdentifier): StateAndRef<HouseDVPState> {
        val criteria = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(id))
        return serviceHub.vaultService.queryBy<HouseDVPState>(criteria = criteria).states.single()
    }

    fun inputStateRefUsingUUID(id: UUID): StateAndRef<HouseDVPState> {
        val criteria = QueryCriteria.LinearStateQueryCriteria(null, listOf(id), null, Vault.StateStatus.UNCONSUMED, null)
        return serviceHub.vaultService.queryBy<HouseDVPState>(criteria = criteria).states.single()
    }

    fun stringToUUID(string: String): UUID {
        return UUID.fromString(string)
    }

    fun stringToLinear (id: String): UniqueIdentifier {
        return UniqueIdentifier.fromString(id)
    }

    fun tokenTypeToCurrency (currency: Amount<TokenType>) : Amount<Currency> {
        return Amount(currency.quantity, Currency.getInstance(currency.token.tokenIdentifier))
    }

     fun currencyToTokenType (currency: Amount<Currency>) : Amount<TokenType> {
        return Amount(currency.quantity, TokenType(currency.token.currencyCode, currency.token.defaultFractionDigits))
    }

    fun getPartyAndToken(party: Party, token: TokenPointer<HouseDVPState>): PartyAndToken<TokenPointer<HouseDVPState>> {
        return PartyAndToken(party, token)
    }
}
