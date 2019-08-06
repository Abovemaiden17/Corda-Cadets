package com.template.flows.DVP

import co.paralleluniverse.fibers.Suspendable
import com.r3.corda.lib.tokens.contracts.utilities.issuedBy
import com.r3.corda.lib.tokens.workflows.flows.rpc.IssueTokens
import com.r3.corda.lib.tokens.workflows.utilities.heldBy
import com.template.DVPstateAndContract.HouseState
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction


@StartableByRPC
@InitiatingFlow
class HouseTokenIssueFlow(val linearId: String,
                          val owner: String) : FlowFunctions() {
    @Suspendable
    override fun call(): SignedTransaction {

        val evolvableTokenType = refCodeUUID(linearId).state.data
        val token = evolvableTokenType.toPointer<HouseState>()
        val houseToken = token issuedBy ourIdentity heldBy stringToParty(owner)
        return subFlow(IssueTokens(listOf(houseToken)))
    }


}