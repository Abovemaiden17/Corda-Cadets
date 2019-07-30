package com.template.flows.houseFlows

import co.paralleluniverse.fibers.Suspendable
import com.google.common.collect.ImmutableList
import com.r3.corda.lib.tokens.contracts.types.TokenPointer
import com.r3.corda.lib.tokens.workflows.flows.rpc.RedeemNonFungibleTokens
import com.template.states.HouseState
import net.corda.core.contracts.LinearPointer
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.Party
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import java.util.*

@InitiatingFlow
@StartableByRPC

class RedeemHouseTokenFlow(private val evolvableTokenId: String,
                           private val issuer: Party) : FlowLogic<SignedTransaction>() {

    @Suspendable
    override fun call() : SignedTransaction {
        val uuid = UUID.fromString(evolvableTokenId)
            // construct queryCriteria to get the created asset on ledger by using LinearStateCriteria
            // get all the unconsumed states from vault_linear_states having uuid
        val queryCriteria = QueryCriteria.LinearStateQueryCriteria(null, ImmutableList.of(uuid), null,
                    Vault.StateStatus.UNCONSUMED, null)
            // use vaultservice to hit the vault using the query criteria
        val stateAndRef = serviceHub.vaultService.queryBy(HouseState::class.java, queryCriteria).states[0]
           // get the state from StateAndRef returned by the query
        val evolvableTokenType = stateAndRef.state.data
        val linearPointer = LinearPointer(evolvableTokenType.linearId, HouseState::class.java)
            // token pointer is a linear pointer to created real estate asset
        val token =  TokenPointer(linearPointer, evolvableTokenType.fractionDigits)
            // redeem token by specifying the issuer
        return subFlow( RedeemNonFungibleTokens(token, issuer))
    }
}