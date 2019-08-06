package com.template.flows.HouseFLOWS

import co.paralleluniverse.fibers.Suspendable
import com.google.common.collect.ImmutableList
import com.r3.corda.lib.tokens.contracts.states.NonFungibleToken
import com.r3.corda.lib.tokens.contracts.types.TokenPointer
import com.r3.corda.lib.tokens.contracts.utilities.issuedBy
import com.r3.corda.lib.tokens.money.FiatCurrency
import com.r3.corda.lib.tokens.workflows.flows.rpc.IssueTokens
import com.r3.corda.lib.tokens.workflows.utilities.heldBy
import com.template.TokenSDKsample.House
import com.template.tokenschedule.UserRegisterState
import net.corda.core.contracts.Amount
import net.corda.core.contracts.LinearPointer
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.Party
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction

@InitiatingFlow
@StartableByRPC
class IssueTokensFlow(private val evolvableTokenId: String,
                       private val recipient: Party) : FlowFunctions(){

@Suspendable
    override fun call(): SignedTransaction {
         val uuid = stringToUUID(evolvableTokenId)

        val queryCriteria = QueryCriteria.LinearStateQueryCriteria(null, ImmutableList.of(uuid), null,
                Vault.StateStatus.UNCONSUMED, null)

        val stateAndRef = serviceHub.vaultService.queryBy(House::class.java, queryCriteria).states[0]
        val evolvableTokenType = stateAndRef.state.data
        val linearPointer = LinearPointer(evolvableTokenType.linearId, House::class.java)
        val token = TokenPointer(linearPointer, evolvableTokenType.fractionDigits)
        val abstrctToken = token issuedBy ourIdentity heldBy recipient


        return subFlow(IssueTokens(listOf(abstrctToken)))
    }



}