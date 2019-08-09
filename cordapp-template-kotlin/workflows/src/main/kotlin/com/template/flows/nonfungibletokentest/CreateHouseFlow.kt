package com.template.flows.nonfungibletokentest

import co.paralleluniverse.fibers.Suspendable
import com.r3.corda.lib.tokens.contracts.states.NonFungibleToken
import com.r3.corda.lib.tokens.contracts.types.IssuedTokenType
import com.r3.corda.lib.tokens.contracts.utilities.getAttachmentIdForGenericParam
import com.r3.corda.lib.tokens.money.FiatCurrency
import com.r3.corda.lib.tokens.workflows.flows.evolvable.CreateEvolvableTokensFlow
import com.r3.corda.lib.tokens.workflows.flows.rpc.IssueTokens
import net.corda.core.contracts.Amount
import net.corda.core.contracts.TransactionState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import java.util.*

@StartableByRPC
class CreateHouseFlow (val owner: String,val address: String, val amount:Long, val currency: String): TokenFunctions()
{
    @Suspendable
    override fun call(): SignedTransaction {
        val notary = serviceHub.networkMapCache.notaryIdentities[0]
        val linearId = UniqueIdentifier.fromString(UUID.randomUUID().toString())
        val transactionState = TransactionState(houseState(linearId),TokenHouseContract.CONTRACT_ID,notary)
        subFlow(CreateEvolvableTokensFlow(transactionState, listOf()))
        val issueHouseToken = IssuedTokenType(ourIdentity,houseState(linearId).toPointer<TokenHouseState>())
        val houseToken  = NonFungibleToken(issueHouseToken,stringtoParty(owner),UniqueIdentifier.fromString(UUID.randomUUID().toString()),issueHouseToken.getAttachmentIdForGenericParam())
        return subFlow(IssueTokens(listOf(houseToken)))
    }

    private fun houseState(linearId: UniqueIdentifier): TokenHouseState
    {
        return TokenHouseState(stringtoParty(owner),address,
                                Amount(amount,FiatCurrency.getInstance(currency)),
                                linearId = linearId ,maintainers = listOf(ourIdentity))
    }
}