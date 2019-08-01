package com.template.flows.houseDVP

import co.paralleluniverse.fibers.Suspendable
import com.r3.corda.lib.tokens.contracts.states.NonFungibleToken
import com.r3.corda.lib.tokens.contracts.types.IssuedTokenType
import com.r3.corda.lib.tokens.contracts.types.TokenType
import com.r3.corda.lib.tokens.contracts.utilities.getAttachmentIdForGenericParam
import com.r3.corda.lib.tokens.money.FiatCurrency
import com.r3.corda.lib.tokens.workflows.flows.rpc.CreateEvolvableTokens
import com.r3.corda.lib.tokens.workflows.flows.rpc.IssueTokens
import com.template.houseDVP.HouseDVPState
import net.corda.core.contracts.Amount
import net.corda.core.contracts.TransactionState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import java.util.*

@InitiatingFlow
@StartableByRPC

class HouseTokenCreateAndIssueFlow (private val owner: String,
                                    private val amount: Long,
                                    private val currency: String,
                                    private val noOfBedRooms: Int,
                                    private val address: String) : HouseDVPFunction() {
    @Suspendable
    override fun call() : SignedTransaction {
        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val houseState = HouseDVPState(stringToParty(owner),0, UniqueIdentifier.fromString(UUID.randomUUID().toString()), listOf(ourIdentity),
                valuation = Amount(amount, FiatCurrency.getInstance(currency)), noOfBedRooms = noOfBedRooms, address = address)
        subFlow(CreateEvolvableTokens(TransactionState(houseState, notary = notary), emptyList()))

        val issuedHouseToken = IssuedTokenType(ourIdentity, houseState.pointer())
        val houseToken = NonFungibleToken(issuedHouseToken,stringToParty(owner), UniqueIdentifier.fromString(UUID.randomUUID().toString()),issuedHouseToken.getAttachmentIdForGenericParam())
        return subFlow(IssueTokens(listOf(houseToken)))
    }
}