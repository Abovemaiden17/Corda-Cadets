//
//import net.corda.core.flows.ReceiveFinalityFlow
//import net.corda.core.flows.FlowException
//import net.corda.core.transactions.SignedTransaction
//import net.corda.core.flows.SignTransactionFlow
//import net.corda.core.flows.SendStateAndRefFlow
//import com.r3.corda.lib.tokens.money.FiatCurrency
//import com.r3.corda.lib.tokens.contracts.states.FungibleToken
//import net.corda.core.contracts.StateAndRef
//import com.r3.corda.lib.tokens.workflows.internal.selection.TokenSelection
//import net.corda.core.contracts.Amount
//import net.corda.core.flows.FlowSession
//import net.corda.core.flows.FlowLogic
//import com.template.flows.DVP.HouseSaleInitiatorFlow
//import net.corda.core.flows.InitiatedBy
//
//import com.r3.corda.lib.tokens.contracts.states.FungibleToken
//import com.r3.corda.lib.tokens.money.FiatCurrency
//import com.r3.corda.lib.tokens.workflows.internal.selection.TokenSelection
//import com.template.flows.DVP.HouseSaleInitiatorFlow
//import net.corda.core.contracts.Amount
//import net.corda.core.contracts.StateAndRef
//import net.corda.core.flows.FlowException
//import net.corda.core.flows.FlowLogic
//import net.corda.core.flows.FlowSession
//import net.corda.core.flows.InitiatedBy
//import net.corda.core.flows.ReceiveFinalityFlow
//import net.corda.core.flows.SendStateAndRefFlow
//import net.corda.core.flows.SignTransactionFlow
//import net.corda.core.transactions.SignedTransaction
//
//@InitiatedBy(HouseSaleInitiatorFlow::class)
//class HouseSaleResponderFlow(private val counterpartySession: FlowSession) : FlowLogic<SignedTransaction>() {
//
//    @Suspendable
//    @Throws(FlowException::class)
//    override fun call(): SignedTransaction {
//
//        /* Recieve the valuation of the house */
//        val price = counterpartySession.receive(Amount<*>::class.java).unwrap<Amount>({ amount -> amount })
//        /* Create instance of the fiat currecy token */
//        val priceToken = Amount(price.quantity, FiatCurrency(price.token))
//
//        /* Create an instance of the TokenSelection object, it is used to select the token from the vault and generate the proposal for the movement of the token
//        *  The constructor takes the service hub to perform vault query, the max-number of retries, the retry sleep interval, and the retry sleep cap interval. This
//        *  is a temporary solution till in-memory token selection in implemented.
//        * */
//        val tokenSelection = TokenSelection(serviceHub, 8, 100, 2000)
//
//        /*
//        *  Generate the move proposal, it returns the input-output pair for the fiat currency transfer, which we need to send to the Initiator.
//        * */
//        val partyAndAmount = PartyAndAmount(counterpartySession.counterparty, priceToken)
//        val inputsAndOutputs = tokenSelection.generateMove(runId.uuid, ImmutableList.of(partyAndAmount), ourIdentity, null)
//
//        /* Call SendStateAndRefFlow to send the inputs to the Initiator*/
//        subFlow<Void>(SendStateAndRefFlow(counterpartySession, inputsAndOutputs.getFirst()))
//        /* Send the output generated from the fiat currency move proposal to the initiator */
//        counterpartySession.send(inputsAndOutputs.getSecond())
//
//        subFlow(object : SignTransactionFlow(counterpartySession) {
//            @Throws(FlowException::class)
//            override fun checkTransaction(stx: SignedTransaction) {
//                // Custom Logic to validate transaction.
//            }
//        })
//        return subFlow(ReceiveFinalityFlow(counterpartySession))
//    }
//}