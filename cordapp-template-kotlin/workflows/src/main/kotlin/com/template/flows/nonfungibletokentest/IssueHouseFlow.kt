package com.template.flows.nonfungibletokentest

import co.paralleluniverse.fibers.Suspendable
import com.google.common.collect.ImmutableList
import com.r3.corda.lib.tokens.contracts.types.TokenPointer
import com.r3.corda.lib.tokens.contracts.utilities.issuedBy
import com.r3.corda.lib.tokens.workflows.flows.rpc.IssueTokens
import com.r3.corda.lib.tokens.workflows.utilities.heldBy
import net.corda.core.contracts.LinearPointer
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.Party
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import java.util.*

@StartableByRPC
class IssueHouseFlow(val linearId: String, val owner: Party): FlowLogic<SignedTransaction>()
{
    @Suspendable
    override fun call(): SignedTransaction {

        val uuid = stringToUUID(linearId)

        val queryCriteria = QueryCriteria.LinearStateQueryCriteria(null, ImmutableList.of(uuid), null,
                Vault.StateStatus.UNCONSUMED, null)

        val stateAndRef = serviceHub.vaultService.queryBy(TokenHouseState::class.java, queryCriteria).states[0]
        val evolvableTokenType = stateAndRef.state.data
        val linearPointer = LinearPointer(evolvableTokenType.linearId, TokenHouseState::class.java)
        val token = TokenPointer(linearPointer, evolvableTokenType.fractionDigits)
        val houseToken = token issuedBy ourIdentity heldBy owner
        return subFlow(IssueTokens(listOf(houseToken)))
    }

    fun stringToUUID(id: String): UUID {
        return UUID.fromString(id)
    }
}
