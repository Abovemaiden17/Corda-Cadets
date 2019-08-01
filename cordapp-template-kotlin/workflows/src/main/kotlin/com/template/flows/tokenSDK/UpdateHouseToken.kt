package com.template.flows.tokenSDK

import co.paralleluniverse.fibers.Suspendable
import com.google.common.collect.ImmutableList
import com.r3.corda.lib.tokens.contracts.types.TokenPointer
import com.r3.corda.lib.tokens.contracts.types.TokenType
import com.r3.corda.lib.tokens.contracts.utilities.issuedBy
import com.r3.corda.lib.tokens.money.FiatCurrency
import com.r3.corda.lib.tokens.workflows.flows.rpc.IssueTokens
import com.r3.corda.lib.tokens.workflows.flows.rpc.UpdateEvolvableToken
import com.r3.corda.lib.tokens.workflows.utilities.heldBy
import net.corda.core.contracts.Amount
import net.corda.core.contracts.LinearPointer
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import java.util.*

@StartableByRPC
@InitiatingFlow

class UpdateHouseToken (val linearId: String,
                        val newValuation: Long,
                        val currency: String) : FlowLogic<SignedTransaction>()
{
    @Suspendable
    override fun call(): SignedTransaction {

        val unique = stringToUniqueIdentifier(linearId)
        val uuid = stringToUUID(linearId)
        val queryCriteria = QueryCriteria.LinearStateQueryCriteria(null, ImmutableList.of(uuid), null,
                Vault.StateStatus.UNCONSUMED, null)

        val stateAndRef = serviceHub.vaultService.queryBy(HouseState::class.java, queryCriteria).states[0]
        val evolvableTokenType = stateAndRef.state.data

        val newState = HouseState(evolvableTokenType.owner,evolvableTokenType.address, Amount(newValuation, FiatCurrency.getInstance(currency)), evolvableTokenType.maintainers, evolvableTokenType.fractionDigits, linearId = unique )
        return subFlow(UpdateEvolvableToken(stateAndRef, newState ))
    }
    fun stringToUUID(id: String): UUID {
        return UUID.fromString(id)
    }
    fun stringToUniqueIdentifier(id: String): UniqueIdentifier{
        return UniqueIdentifier.fromString(id)
    }
}