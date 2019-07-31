package com.template.flows.nonfungibletokentest

import com.r3.corda.lib.tokens.contracts.states.FungibleToken
import com.r3.corda.lib.tokens.workflows.flows.move.addMoveNonFungibleTokens
import com.r3.corda.lib.tokens.workflows.flows.move.addMoveTokens
import com.r3.corda.lib.tokens.workflows.types.PartyAndToken
import com.r3.corda.lib.tokens.workflows.utilities.getPreferredNotary
import jdk.nashorn.internal.parser.Token
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.FlowSession
import net.corda.core.identity.Party
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import javax.annotation.Signed

abstract class TokenFunctions : FlowLogic<SignedTransaction>()
{
    fun inputStateRef(id: UniqueIdentifier): StateAndRef<TokenHouseState> {
        val criteria = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(id))
        return serviceHub.vaultService.queryBy<TokenHouseState>(criteria = criteria).states.single()
    }

    fun stringToLinear(id: String): UniqueIdentifier {
        return UniqueIdentifier.fromString(id)
    }
    fun moveNonFungibleToken(houseId: String,buyer: Party): TransactionBuilder
    {
        return addMoveNonFungibleTokens(getbuilder(), serviceHub, house(houseId).toPointer<TokenHouseState>(), buyer)
    }
    fun moveTokens(input: List<StateAndRef<FungibleToken>>,output: List<FungibleToken>):TransactionBuilder
    {
        return addMoveTokens(getbuilder(),input,output)
    }
    fun getbuilder(): TransactionBuilder
    {
        return TransactionBuilder(notary = getPreferredNotary(serviceHub))
    }
    fun house(houseId: String): TokenHouseState
    {
        return inputStateRef(stringToLinear(houseId)).state.data
    }
}