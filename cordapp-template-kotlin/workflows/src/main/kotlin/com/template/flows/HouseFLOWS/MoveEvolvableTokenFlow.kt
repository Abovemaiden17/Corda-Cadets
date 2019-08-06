package com.template.flows.HouseFLOWS

import co.paralleluniverse.fibers.Suspendable
import com.google.common.collect.ImmutableList
import com.r3.corda.lib.tokens.contracts.types.TokenPointer
import com.r3.corda.lib.tokens.workflows.flows.rpc.MoveNonFungibleTokens
import com.r3.corda.lib.tokens.workflows.types.PartyAndToken
import com.template.TokenSDKsample.House
import net.corda.core.contracts.LinearPointer
import net.corda.core.flows.FlowException
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.Party
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import java.util.*

@StartableByRPC
class MoveEvolvableTokenFlow(private val evolvableTokenId: String,
                             private val recipient: Party) : FlowLogic<SignedTransaction>() {

    @Suspendable
    @Throws(FlowException::class)
    override fun call(): SignedTransaction {
        val uuid = UUID.fromString(evolvableTokenId)

        val queryCriteria = QueryCriteria.LinearStateQueryCriteria(null, ImmutableList.of(uuid), null,
                Vault.StateStatus.UNCONSUMED, null)
        val stateAndRef = serviceHub.vaultService.queryBy(House::class.java, queryCriteria).states[0]
        val evolvableTokenType = stateAndRef.state.data
        val linearPointer = LinearPointer(evolvableTokenType.linearId, House::class.java)
        val token = TokenPointer(linearPointer, evolvableTokenType.fractionDigits)
        val partyAndToken = PartyAndToken(recipient, token)
        return subFlow<Any>(MoveNonFungibleTokens(partyAndToken)) as SignedTransaction
    }
}