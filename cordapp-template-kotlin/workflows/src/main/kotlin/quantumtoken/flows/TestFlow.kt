package quantumtoken.flows

import com.r3.corda.lib.tokens.contracts.states.FungibleToken
import com.r3.corda.lib.tokens.contracts.types.TokenType
import com.r3.corda.lib.tokens.contracts.utilities.withNotary
import com.r3.corda.lib.tokens.money.FiatCurrency
import com.r3.corda.lib.tokens.money.GBP
import com.r3.corda.lib.tokens.workflows.flows.move.addMoveTokens
import com.r3.corda.lib.tokens.workflows.flows.rpc.CreateEvolvableTokens
import com.r3.corda.lib.tokens.workflows.flows.rpc.IssueTokens
import com.r3.corda.lib.tokens.workflows.internal.flows.distribution.UpdateDistributionListFlow
import com.r3.corda.lib.tokens.workflows.internal.flows.finality.ObserverAwareFinalityFlow
import com.r3.corda.lib.tokens.workflows.internal.flows.finality.ObserverAwareFinalityFlowHandler
import com.r3.corda.lib.tokens.workflows.internal.selection.TokenSelection
import com.r3.corda.lib.tokens.workflows.types.PartyAndAmount
import com.r3.corda.lib.tokens.workflows.types.PartyAndToken
import com.r3.corda.lib.tokens.workflows.utilities.ourSigningKeys
import net.corda.confidential.IdentitySyncFlow
import net.corda.core.contracts.Amount
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.*
//import net.corda.core.flows.IdentitySyncFlow
import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.unwrap
import quantumtoken.functions.TestFunctions
import quantumtoken.states.TestState

@InitiatingFlow
@StartableByRPC
class TestFlow (private val house: TestState,
                private val newOwner: Party): TestFunctions()
{
    override fun call(): SignedTransaction
    {
        val housePtr = house.toPointer<TestState>()
        val txBuilder = TransactionBuilder(notary = serviceHub.networkMapCache.notaryIdentities.first())
        // Initiate new flow session. If this flow is supposed to be called as inline flow, then session should have been already passed.
        val session = initiateFlow(newOwner)

        // Ask for input stateAndRefs - send notification with the amount to exchange.
        session.send(PriceNotification(house.valuation))

        // Receive GBP states back.
        val inputs = subFlow(ReceiveStateAndRefFlow<FungibleToken>(session))

        // Receive outputs.
        val outputs = session.receive<List<FungibleToken>>().unwrap { it }
        addMoveTokens(txBuilder, inputs, outputs)
        subFlow(IdentitySyncFlow.Send(session, txBuilder.toWireTransaction(serviceHub)))

        // Because states on the transaction can have confidential identities on them, we need to sign them with corresponding keys.
        val ourSigningKeys = txBuilder.toLedgerTransaction(serviceHub).ourSigningKeys(serviceHub)
        val initialStx = verifyAndSign(txBuilder, ourSigningKeys)

        // Collect signatures from the new house owner.
        val stx = collectSignature(initialStx, listOf(session), ourSigningKeys)
        subFlow(UpdateDistributionListFlow(stx))
        return subFlow(ObserverAwareFinalityFlow(stx, listOf(session)))
    }
}

@InitiatedBy(TestFlow::class)
class TestFlowResponder(private val otherSession: FlowSession): FlowLogic<Unit>()
{
    override fun call() {
        // Receive notification with house price.
        val priceNotification = otherSession.receive<PriceNotification>().unwrap { it }

        // Generate fresh key, possible change outputs will belong to this key.
        val changeHolder = serviceHub.keyManagementService.freshKeyAndCert(ourIdentityAndCert, false).party.anonymise()

        // Chose state and refs to send back.
        val (inputs, outputs) = TokenSelection(serviceHub).generateMove(
                lockId = runId.uuid,
                partyAndAmounts = listOf(PartyAndAmount(otherSession.counterparty, priceNotification.amount)),
                changeHolder = changeHolder
        )
        subFlow(SendStateAndRefFlow(otherSession, inputs))
        otherSession.send(outputs)
        subFlow(IdentitySyncFlow.Receive(otherSession))
        subFlow(object : SignTransactionFlow(otherSession) {
            override fun checkTransaction(stx: SignedTransaction) {
                // We should perform some basic sanity checks before signing the transaction. This step was omitted for simplicity.
            }
        })
        subFlow(ObserverAwareFinalityFlowHandler(otherSession))
    }
}

@CordaSerializable
data class PriceNotification(val amount: Amount<TokenType>)