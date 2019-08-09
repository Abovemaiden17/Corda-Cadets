package com.template.flows.smx

import co.paralleluniverse.fibers.Suspendable
import com.template.states.SMXProductState
import com.template.types.SMXProductContract
import net.corda.core.contracts.Command
import net.corda.core.flows.*
import net.corda.core.node.services.Vault
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import java.time.Instant

@InitiatingFlow
@StartableByRPC
class SMXProductRegisterFlow (val serialNumber: Int,val name: String): FlowLogic<SignedTransaction>()
{
    @Suspendable
    override fun call(): SignedTransaction {
        val transaction = verifyAndSign(transaction(outputState()))
        val session : List<FlowSession> = emptyList()
        return subFlow(FinalityFlow(transaction,session))
    }
    private fun transaction(state: SMXProductState): TransactionBuilder
    {
        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val command = Command(SMXProductContract.Commands.Register(),ourIdentity.owningKey)
        val builder = TransactionBuilder(notary = notary)
                .addCommand(command)
                .addOutputState(state = state,contract = SMXProductContract.ID_Contracts)
        return builder
    }
    private fun outputState(): SMXProductState
    {
        val criteria = QueryCriteria.VaultQueryCriteria(status = Vault.StateStatus.UNCONSUMED)
        val queryCriteria = serviceHub.vaultService.queryBy<SMXProductState>(criteria = criteria).states.size
        return SMXProductState(id = queryCriteria + 1,
                productName = name,
                smx_serial_num = serialNumber,
                owner = ourIdentity,
                date = Instant.now(),
                participants = listOf(ourIdentity))
    }
    private fun verifyAndSign(transaction: TransactionBuilder): SignedTransaction
    {
        transaction.verify(serviceHub)
        return serviceHub.signInitialTransaction(transaction)
    }
    @Suspendable
    fun recordUpdate(transaction: SignedTransaction,session: List<FlowSession>): SignedTransaction
            = subFlow(FinalityFlow(transaction = transaction,sessions =  session))
}