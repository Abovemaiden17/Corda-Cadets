package com.template.flows.corexchange

import co.paralleluniverse.fibers.Suspendable
import com.r3.corda.lib.tokens.contracts.types.TokenType
import com.sun.corba.se.pept.transport.Acceptor
import com.template.states.UserState
import com.template.types.UserContract
import jdk.nashorn.internal.ir.LexicalContextNode
import net.corda.core.contracts.Amount
import net.corda.core.contracts.Command
import net.corda.core.flows.*
import net.corda.core.identity.CordaX500Name
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder

@InitiatingFlow
@StartableByRPC
class RegisterUserFlow(val name: String, val wallet: List<Amount<TokenType>>): FunctionFlows()
{
    @Suspendable
    override fun call(): SignedTransaction {
        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val transaction = verifyAndSign(transaction(register(),notary))
        val session  = emptyList<FlowSession>()
        val stx = subFlow(CollectSignaturesFlow(transaction, session))
        subFlow(BroadcastFlow(stx))
        return recordRegister(stx,session)
    }
    private fun register(): UserState
    {
        return UserState(name,wallet.toMutableList(), listOf(ourIdentity))
    }
    private fun transaction(state: UserState,notary: Party)
            = TransactionBuilder(notary = notary).apply {
        val command = Command(UserContract.Commands.Register(),ourIdentity.owningKey)
        addOutputState(state,UserContract.ID_Contracts)
        addCommand(command)
    }
}