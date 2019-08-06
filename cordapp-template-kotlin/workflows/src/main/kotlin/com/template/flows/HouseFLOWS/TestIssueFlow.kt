package com.template.flows.HouseFLOWS

import co.paralleluniverse.fibers.Suspendable
import com.r3.corda.lib.tokens.contracts.states.NonFungibleToken
import com.r3.corda.lib.tokens.contracts.utilities.issuedBy
import com.r3.corda.lib.tokens.workflows.flows.rpc.ConfidentialIssueTokens
import com.r3.corda.lib.tokens.workflows.utilities.heldBy
import com.template.TokenSDKsample.House
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction


@InitiatingFlow
@StartableByRPC
class TestIssueFlow(private val evolvableTokenId: String,
                    private val recipient: Party) : FlowFunctions() {

    @Suspendable
    override fun call(): SignedTransaction {

        val house: House = outputState()
        val counterParty: Party = recipient
        val issuerParty: Party = ourIdentity
        val housePtr = house.toPointer<House>()
        // Create NonFungibleToken referencing house with Alice party as an owner.
        val houseToken: NonFungibleToken = housePtr issuedBy issuerParty heldBy counterParty
        return subFlow(ConfidentialIssueTokens(listOf(houseToken)))

    }

    private fun outputState(): House {
        val input = inputStateRef(id = stringToLinear(evolvableTokenId)).state.data
        return House(
                "Bahay",
                valuation = input.valuation,
                maintainers = listOf(ourIdentity),
                linearId = UniqueIdentifier()
        )
    }


}