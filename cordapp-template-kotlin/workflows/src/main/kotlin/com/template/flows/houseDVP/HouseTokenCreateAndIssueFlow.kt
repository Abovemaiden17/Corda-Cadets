package com.template.flows.houseDVP

import co.paralleluniverse.fibers.Suspendable
import com.google.common.collect.ImmutableList
import com.r3.corda.lib.tokens.contracts.states.NonFungibleToken
import com.r3.corda.lib.tokens.contracts.types.IssuedTokenType
import com.r3.corda.lib.tokens.contracts.types.TokenPointer
import com.r3.corda.lib.tokens.contracts.utilities.getAttachmentIdForGenericParam
import com.r3.corda.lib.tokens.workflows.flows.rpc.CreateEvolvableTokens
import com.r3.corda.lib.tokens.workflows.flows.rpc.IssueTokens
import com.template.houseDVP.HouseDVPState
import com.template.states.HouseState
import net.corda.core.contracts.Amount
import net.corda.core.contracts.TransactionState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.Party
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import java.util.*

@InitiatingFlow
@StartableByRPC

class HouseTokenCreateAndIssueFlow (private val owner: Party,
                                    private val valuation: Amount<Currency>,
                                    private val noOfBedRooms: Int,
                                    private val address: String) : HouseDVPFunction() {
    @Suspendable
    override fun call() : SignedTransaction {
        val notary = serviceHub.networkMapCache.notaryIdentities[0]
        val issuer = ourIdentity
        val houseState = HouseDVPState(owner,0, UniqueIdentifier.fromString(UUID.randomUUID().toString()),ImmutableList.of(issuer),
                valuation, noOfBedRooms, address)
        subFlow(CreateEvolvableTokens(TransactionState(houseState,notary = notary)))

        val issuedHouseToken = IssuedTokenType(issuer, houseState.pointer())
        val houseToken = NonFungibleToken(issuedHouseToken,owner, UniqueIdentifier.fromString(UUID.randomUUID().toString()),issuedHouseToken.getAttachmentIdForGenericParam())
        return subFlow(IssueTokens(listOf(houseToken)))
    }
}