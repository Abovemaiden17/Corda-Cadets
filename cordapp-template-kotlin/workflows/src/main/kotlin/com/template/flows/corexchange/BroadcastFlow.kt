package com.template.flows.corexchange

import co.paralleluniverse.fibers.Suspendable
import com.r3.corda.lib.tokens.contracts.states.FungibleToken
import com.r3.corda.lib.tokens.workflows.flows.move.addMoveTokens
import com.r3.corda.lib.tokens.workflows.internal.flows.distribution.UpdateDistributionListFlow
import com.r3.corda.lib.tokens.workflows.internal.flows.finality.ObserverAwareFinalityFlow
import com.r3.corda.lib.tokens.workflows.internal.flows.finality.ObserverAwareFinalityFlowHandler
import com.r3.corda.lib.tokens.workflows.internal.selection.TokenSelection
import com.r3.corda.lib.tokens.workflows.types.PartyAndAmount
import com.r3.corda.lib.tokens.workflows.utilities.ourSigningKeys
import com.template.flows.nonfungibletokentest.PriceNotification
import com.template.states.UserState
import com.template.types.UserContract
import javafx.geometry.VPos
import net.corda.confidential.IdentitySyncFlow
import net.corda.core.contracts.Command
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.TransactionState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.*
import net.corda.core.identity.CordaX500Name
import net.corda.core.identity.Party
import net.corda.core.internal.FlowIORequest
import net.corda.core.node.StatesToRecord
import net.corda.core.node.services.Vault
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.unwrap
import org.intellij.lang.annotations.Flow
import java.lang.IllegalStateException

@InitiatingFlow
class BroadcastFlow(private val stx: SignedTransaction):FlowLogic<Unit>()
{
    @Suspendable
    override fun call()
    {
            val phpSession = initiateFlow(issuePHP())
            val usdSession = initiateFlow(issueUSD())
            subFlow(SendTransactionFlow(phpSession,stx))
            subFlow(SendTransactionFlow(usdSession,stx))

    }
    fun issuePHP(): Party {
        val phpIssuer = serviceHub.identityService.partiesFromName("IssuerPHP", false).singleOrNull()
                ?: throw IllegalArgumentException("No match found for IssuerPHP")
        return phpIssuer
    }
    fun issueUSD(): Party {
        val usdIssuer = serviceHub.identityService.partiesFromName("IssuerUSD", false).singleOrNull()
                ?: throw IllegalArgumentException("No match found for IssuerUSD")

        return usdIssuer
    }

}

@InitiatedBy(BroadcastFlow::class)
class GetUsersDataFlowResponder(private val session: FlowSession) : FlowLogic<Unit>()
{
    @Suspendable
    override fun call() {
    subFlow(ReceiveTransactionFlow(session,statesToRecord = StatesToRecord.ALL_VISIBLE))
    }
}