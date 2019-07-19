package com.template.flows.tokentest

import co.paralleluniverse.fibers.Suspendable
import com.google.common.collect.ImmutableList
import com.r3.corda.lib.tokens.contracts.states.AbstractToken
import com.r3.corda.lib.tokens.contracts.states.EvolvableTokenType
import com.r3.corda.lib.tokens.contracts.states.NonFungibleToken
import com.r3.corda.lib.tokens.contracts.types.IssuedTokenType
import com.r3.corda.lib.tokens.workflows.flows.rpc.IssueTokens
import net.corda.core.transactions.SignedTransaction
import com.r3.corda.lib.tokens.contracts.types.TokenPointer
import com.r3.corda.lib.tokens.contracts.utilities.issuedBy
import com.r3.corda.lib.tokens.workflows.utilities.heldBy
import net.corda.core.contracts.LinearPointer
import net.corda.core.contracts.StateAndRef
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.flows.FlowException
import net.corda.core.identity.Party
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.StartableByRPC
import java.util.*


@StartableByRPC
class IssueTokenFlow(val evolvableTokenId: String,val recipient: Party) : FlowLogic<SignedTransaction>() {

    @Suspendable
    @Throws(FlowException::class)
    override fun call(): SignedTransaction {
        // uuid column in vault_linear_states contains the uuid of the created asset on ledger
        val uuid = UUID.fromString(evolvableTokenId)
        // construct queryCriteria to get the created asset on ledger by using LinearStateCriteria
        // get all the unconsumed states from vault_linear_states having uuid
        val queryCriteria = QueryCriteria.LinearStateQueryCriteria(null, ImmutableList.of(uuid), null,
                Vault.StateStatus.UNCONSUMED, null)
        // use vaultservice to hit the vault using the query criteria
        val stateAndRef = serviceHub.vaultService.queryBy(HouseState::class.java, queryCriteria).states[0]
        // get the state from StateAndRef returned by the query
        val evolvableTokenType = stateAndRef.state.data
        val linearPointer = LinearPointer(evolvableTokenType.linearId, HouseState::class.java!!)
        // token pointer is a linear pointer to created real estate asset
        val token = TokenPointer(linearPointer, evolvableTokenType.fractionDigits)
        val abstractToken : NonFungibleToken = token issuedBy ourIdentity heldBy  recipient
        // issue token stating issuer who is getOurIdentity() for this example, recipient will be the holder of the token
        return subFlow(IssueTokens(listOf(abstractToken),listOf(ourIdentity)))
    }


}