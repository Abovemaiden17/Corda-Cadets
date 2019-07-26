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
import com.r3.corda.lib.tokens.workflows.flows.issue.addIssueTokens
import com.r3.corda.lib.tokens.workflows.internal.flows.distribution.UpdateDistributionListFlow
import com.r3.corda.lib.tokens.workflows.internal.flows.finality.ObserverAwareFinalityFlow
import com.r3.corda.lib.tokens.workflows.utilities.addTokenTypeJar
import com.r3.corda.lib.tokens.workflows.utilities.getPreferredNotary
import com.r3.corda.lib.tokens.workflows.utilities.heldBy
import net.corda.core.contracts.Command
import net.corda.core.contracts.LinearPointer
import net.corda.core.contracts.StateAndRef
import net.corda.core.flows.*
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.identity.Party
import net.corda.core.transactions.TransactionBuilder
import java.util.*
import kotlin.math.sign


@StartableByRPC
class IssueTokenFlow(val evolvableTokenId: String,val recipient: Party) : FlowLogic<SignedTransaction>() {

    @Suspendable
    @Throws(FlowException::class)
    override fun call(): SignedTransaction {
        val uuid = UUID.fromString(evolvableTokenId)
        val queryCriteria = QueryCriteria.LinearStateQueryCriteria(null, ImmutableList.of(uuid), null,
                Vault.StateStatus.UNCONSUMED, null)
        val stateAndRef = serviceHub.vaultService.queryBy(HouseState::class.java, queryCriteria).states[0]
        val evolvableTokenType = stateAndRef.state.data
        val linearPointer = LinearPointer(evolvableTokenType.linearId, HouseState::class.java!!)
        val token = TokenPointer(linearPointer, evolvableTokenType.fractionDigits)
        val abstractToken : NonFungibleToken = token issuedBy ourIdentity heldBy  recipient
        return subFlow(IssueTokens(listOf(abstractToken),listOf(ourIdentity)))
//        val transactionBuilder = TransactionBuilder(notary = stateAndRef.state.notary)
//        addIssueTokens(transactionBuilder,abstractToken)
//        addTokenTypeJar(abstractToken,transactionBuilder)
//        val session = initiateFlow(recipient)
//        val signedTransaction = subFlow(ObserverAwareFinalityFlow(transactionBuilder, listOf(session)))
//        subFlow(UpdateDistributionListFlow(signedTransaction))
//        return signedTransaction
    }


}