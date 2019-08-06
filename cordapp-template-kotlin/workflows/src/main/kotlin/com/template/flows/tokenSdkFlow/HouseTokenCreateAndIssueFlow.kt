package com.template.flows.tokenSdkFlow

import co.paralleluniverse.fibers.Suspendable
import com.google.common.collect.ImmutableList
import com.r3.corda.lib.tokens.contracts.states.NonFungibleToken
import com.r3.corda.lib.tokens.contracts.types.IssuedTokenType
import com.r3.corda.lib.tokens.contracts.utilities.getAttachmentIdForGenericParam
import com.r3.corda.lib.tokens.money.FiatCurrency
import com.r3.corda.lib.tokens.workflows.flows.rpc.CreateEvolvableTokens
import com.r3.corda.lib.tokens.workflows.flows.rpc.IssueTokens
import com.template.tokenSdk.HouseContract
import com.template.tokenSdk.HouseState
import net.corda.core.contracts.Amount
import net.corda.core.contracts.TransactionState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.FlowException
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import java.util.*


/**
 * Call CreateEvolvableToken Flow to create the token.
 */
@StartableByRPC
class HouseTokenCreateAndIssueFlow(val amount: Long, val currency: String, val owner: String) : FlowLogic<SignedTransaction>() {

    @Suspendable
    @Throws(FlowException::class)
    override fun call(): SignedTransaction {
        // get the notary
        val notary = serviceHub.networkMapCache.notaryIdentities[0]
        val issuer = ourIdentity
        // create real estate asset object
        val evolvableTokenType = HouseState(
                valuation = Amount(amount, FiatCurrency.getInstance(currency)),
                maintainers = ImmutableList.of(issuer),
                linearId =  UniqueIdentifier.fromString(UUID.randomUUID().toString())
        )
        //create TransactionState which is a wrapper around real estate asset containing
        // additional platform-level state information and contract information
        val transactionState = TransactionState(evolvableTokenType, HouseContract.ID, notary)
        // invoke built in CreateEvolvableToken flow which creates the asset entry on the ledger
        subFlow(CreateEvolvableTokens(transactionState))
/*
        * Create an instance of IssuedTokenType, it is used by our Non-Fungible token which would be issued to the owner. Note that the IssuedTokenType takes
        * a TokenPointer as an input, since EvolvableTokenType is not TokenType, but is a LinearState. This is done to separate the state info from the token
        * so that the state can evolve independently.
        * IssuedTokenType is a wrapper around the TokenType and the issuer.
        * */
        val issuedHouseToken = IssuedTokenType(issuer, HouseState.toPointer(evolvableTokenType))
        /* Create an instance of the non-fungible house token with the owner as the token holder. The last paramter is a hash of the jar containing the TokenType, use the helper function to fetch it. */
        val counterRef = serviceHub.identityService.partiesFromName(owner, false).singleOrNull()
                ?: throw IllegalArgumentException("No match found for Owner $owner.")
        val houseToken = NonFungibleToken(issuedHouseToken, counterRef, UniqueIdentifier.fromString(UUID.randomUUID().toString()), HouseState.toPointer(evolvableTokenType).getAttachmentIdForGenericParam())
        /* Issue the house token by calling the IssueToken flow provided with the TokenSDK */
        return subFlow(IssueTokens(listOf(houseToken)))
    }
}
