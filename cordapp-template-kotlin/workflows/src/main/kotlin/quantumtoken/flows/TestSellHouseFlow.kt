package quantumtoken.flows

import co.paralleluniverse.fibers.Suspendable
import com.r3.corda.lib.tokens.contracts.states.FungibleToken
import com.r3.corda.lib.tokens.contracts.types.TokenType
import com.r3.corda.lib.tokens.workflows.flows.move.addMoveNonFungibleTokens
import com.r3.corda.lib.tokens.workflows.flows.move.addMoveTokens
import com.r3.corda.lib.tokens.workflows.internal.flows.distribution.UpdateDistributionListFlow
import com.r3.corda.lib.tokens.workflows.internal.flows.finality.ObserverAwareFinalityFlow
import com.r3.corda.lib.tokens.workflows.internal.flows.finality.ObserverAwareFinalityFlowHandler
import com.r3.corda.lib.tokens.workflows.internal.selection.TokenSelection
import com.r3.corda.lib.tokens.workflows.types.PartyAndAmount
import com.r3.corda.lib.tokens.workflows.utilities.ourSigningKeys
import net.corda.confidential.IdentitySyncFlow
import net.corda.core.contracts.Amount
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.*
import net.corda.core.serialization.CordaSerializable
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.unwrap
import quantumtoken.functions.TestFunctions
import quantumtoken.states.HouseState

@CordaSerializable
data class PriceNotification(val amount: Amount<TokenType>)

@InitiatingFlow
@StartableByRPC
class TestSellHouseFlow (private val buyer: String,
                         private val evolvableTokenId: UniqueIdentifier): TestFunctions()
{
    @Suspendable
    override fun call(): SignedTransaction
    {
        // OurIdentity Session
        val evolvableTokenType = inputStateRefUsingLinearID(evolvableTokenId).state.data
        val notary = inputStateRefUsingLinearID(evolvableTokenId).state.notary
        val txBuilder = TransactionBuilder(notary = notary)
        addMoveNonFungibleTokens(
                txBuilder,
                serviceHub,
                evolvableTokenType.toPointer<HouseState>(),
                stringToParty(buyer)
        )

        // Buyer Session
        val session = initiateFlow(stringToParty(buyer))
        session.send(PriceNotification(evolvableTokenType.valuation))
        val inputs = subFlow(ReceiveStateAndRefFlow<FungibleToken>(session))
        val moneyReceived = session.receive<List<FungibleToken>>().unwrap { it }
        addMoveTokens(
                txBuilder,
                inputs,
                moneyReceived)
        subFlow(IdentitySyncFlow.Send(
                session,
                txBuilder.toWireTransaction(serviceHub))
        )

        // Signing of transaction
        val ourSigningKeys = txBuilder.toLedgerTransaction(serviceHub).ourSigningKeys(serviceHub)
        val initialStx = serviceHub.signInitialTransaction(txBuilder, signingPubKeys = ourSigningKeys)
        val stx = collectSignatureWithPubKeys(initialStx, listOf(session), ourSigningKeys)
        subFlow(UpdateDistributionListFlow(stx))
        return subFlow(ObserverAwareFinalityFlow(stx, listOf(session)))
    }
}

@InitiatedBy(TestSellHouseFlow::class)
class TestSellHouseFlowResponder(val flowSession: FlowSession): FlowLogic<Unit>()
{
    @Suspendable
    override fun call()
    {
        // Receive notification with house price.
        val priceNotification = flowSession.receive<PriceNotification>().unwrap { it }
        // Generate fresh key, possible change outputs will belong to this key.
        val changeHolder = serviceHub.keyManagementService.freshKeyAndCert(ourIdentityAndCert, false).party.anonymise()
        // Chose state and refs to send back.
        val (inputs, outputs) = TokenSelection(serviceHub).generateMove(
                lockId = runId.uuid,
                partyAndAmounts = listOf(PartyAndAmount(flowSession.counterparty, priceNotification.amount)),
                changeHolder = changeHolder
        )
        subFlow(SendStateAndRefFlow(flowSession, inputs))
        flowSession.send(outputs)
        subFlow(IdentitySyncFlow.Receive(flowSession))
        subFlow(object : SignTransactionFlow(flowSession) {
            override fun checkTransaction(stx: SignedTransaction) {
                // We should perform some basic sanity checks before signing the transaction. This step was omitted for simplicity.
            }
        })
        subFlow(ObserverAwareFinalityFlowHandler(flowSession))
    }
}