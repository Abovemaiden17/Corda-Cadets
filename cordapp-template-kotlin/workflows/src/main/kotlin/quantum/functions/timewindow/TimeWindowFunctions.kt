package quantum.functions.timewindow

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.CollectSignaturesFlow
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.FlowSession
import net.corda.core.identity.Party
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import quantum.states.TimeWindowState
import java.time.Instant

abstract class TimeWindowFunctions : FlowLogic<SignedTransaction>()
{
    fun verifyAndSign(transaction: TransactionBuilder): SignedTransaction {

        transaction.verify(serviceHub)
        return serviceHub.signInitialTransaction(transaction)
    }

    @Suspendable
    fun collectSignature(
            transaction: SignedTransaction,
            sessions: List<FlowSession>
    ): SignedTransaction = subFlow(CollectSignaturesFlow(transaction, sessions))

    @Suspendable
    fun recordTransactionWithOtherParty(transaction: SignedTransaction, sessions: List<FlowSession>) : SignedTransaction {
        return subFlow(FinalityFlow(transaction, sessions))
    }

    fun stringToParty(name: String): Party {
        return serviceHub.identityService.partiesFromName(name, false).singleOrNull()
                ?: throw IllegalArgumentException("No match found for $name")
    }

    fun inputStateRef(id: UniqueIdentifier): StateAndRef<TimeWindowState> {
        val criteria = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(id))
        return serviceHub.vaultService.queryBy<TimeWindowState>(criteria = criteria).states.single()
    }

    fun getTime(id: UniqueIdentifier): Instant {
        val outputStateRef = net.corda.core.contracts.StateRef(txhash = inputStateRef(id).ref.txhash, index = 0)
        val queryCriteria = QueryCriteria.VaultQueryCriteria(stateRefs = listOf(outputStateRef))
        val results = serviceHub.vaultService.queryBy<TimeWindowState>(queryCriteria)
        return results.statesMetadata.single().recordedTime
    }
}