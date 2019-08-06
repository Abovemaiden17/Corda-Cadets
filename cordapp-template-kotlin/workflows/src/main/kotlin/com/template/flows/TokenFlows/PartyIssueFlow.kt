//package com.template.flows.TokenFlows
//
//import co.paralleluniverse.fibers.Suspendable
//import com.google.common.collect.ImmutableList
//import com.r3.corda.lib.tokens.contracts.types.TokenPointer
//import com.r3.corda.lib.tokens.contracts.utilities.issuedBy
//import com.r3.corda.lib.tokens.workflows.flows.rpc.CreateEvolvableTokens
//import com.r3.corda.lib.tokens.workflows.utilities.heldBy
//import com.template.Token.TokenContract
//import com.template.Token.TokenState
//
//import net.corda.core.contracts.LinearPointer
//import net.corda.core.contracts.TransactionState
//
//import net.corda.core.flows.*
//import net.corda.core.identity.Party
//import net.corda.core.node.services.Vault
//import net.corda.core.node.services.vault.QueryCriteria
//import net.corda.core.transactions.SignedTransaction
//import net.corda.core.utilities.unwrap
//
//
//@InitiatingFlow
//@StartableByRPC
//class PartyIssueFlow(private val evolvableTokenId: String,
//                     private val recipient: Party) : FlowFunctions() {
//
//    @Suspendable
//    override fun call() : SignedTransaction {
//
//
//        progressTracker.currentStep = GENERATING_TRANSACTION
//        progressTracker.currentStep = VERIFYING_TRANSACTION
//        progressTracker.currentStep = SIGNING_TRANSACTION
//
//
//        val uuid = stringToUUID(evolvableTokenId)
//
//        val queryCriteria = QueryCriteria.LinearStateQueryCriteria(null, ImmutableList.of(uuid), null,
//                Vault.StateStatus.UNCONSUMED, null)
//
//        val stateAndRef = serviceHub.vaultService.queryBy(TokenState::class.java, queryCriteria).states[0]
//        val evolvableTokenType = stateAndRef.state.data
//        val linearPointer = LinearPointer(evolvableTokenType.linearId, TokenState::class.java)
//        val token = TokenPointer(linearPointer, evolvableTokenType.fractionDigits)
////        val abstrctToken = token issuedBy ourIdentity heldBy recipient
//
//        val spySession = initiateFlow(recipient)
////        val sessions = initiateFlow(counterRef) // empty because the owner's signature is just needed
//
//        val state = TransactionState(outputState(), TokenContract.tokenID, notary = serviceHub.networkMapCache.notaryIdentities.first())
////        sessions.send(true)
//
//        val transactionSignedByParties =   subFlow(CreateEvolvableTokens(state))
////        spySession.send(true)
////        val transactionSignedByParties = collectSignature(transaction = tx, sessions = listOf(sessions))
//
//        progressTracker.currentStep = FINALISING_TRANSACTION
//        logger.info("The user must verify the issued token within 20seconds")
//        return recordTransaction(transaction = transactionSignedByParties, sessions = listOf(spySession))
//    }
//
//    private fun outputState(): TokenState {
////        val counterRef = stringToParty(counterParty)
//        val linearId = stringToLinear(evolvableTokenId)
//        val input = inputStateRef(linearId).state.data
//        return TokenState()
//    }
//    /****
//     *SPY*
//     ****/
////    private fun transaction(spy: Party) =
////            TransactionBuilder(notary= inputStateRef(linearId).state.notary).apply {
////                //                val ourTimeWindow: TimeWindow = TimeWindow.fromStartAndDuration(serviceHub.clock.instant(), 5.seconds)
////                if(inputStateRef(linearId).state.data.approve == false){
//////                    val counterRef = stringToParty(counterParty)
////                    val spycmd = Command(TokenContract.Commands.Send(), listOf(ourIdentity.owningKey))
////                    val spiedOnMessage = outputState().copy(participants = outputState().participants + spy)
////                    addInputState(inputStateRef(linearId))
////                    addOutputState(spiedOnMessage, tokenID)
////                    addCommand(spycmd)
////                }
////                else{
////                    val counterRef = stringToParty(counterParty)
////                    val spycmd = Command(TokenContract.Commands.Send(), listOf(ourIdentity.owningKey,counterRef.owningKey))
////                    val spiedOnMessage = outputState().copy(participants = outputState().participants + spy)
////                    addInputState(inputStateRef(linearId))
////                    addOutputState(spiedOnMessage, tokenID)
////                    addCommand(spycmd)
////                }
////
//////                setTimeWindow(ourTimeWindow)
////
////            }
//    /***/
//}
//
//@InitiatedBy(PartyIssueFlow::class)
//class PartyIssueFlowResponder(val flowSession: FlowSession): FlowLogic<SignedTransaction>() {
//
//    @Suspendable
//    override fun call(): SignedTransaction {
//
//        // receive the flag
//        val needsToSignTransaction = flowSession.receive<Boolean>().unwrap { it }
//        // only sign if instructed to do so
//        if (needsToSignTransaction) {
//            subFlow(object : SignTransactionFlow(flowSession) {
//                override fun checkTransaction(stx: SignedTransaction) { }
//            })
//        }
//        // always save the transaction
////        val dataTransfered = flowSession.receive(TokenState::class.java).unwrap { it }
////        subFlow(VerifyTokenIssuedFlow())
//
//        return subFlow(ReceiveFinalityFlow(otherSideSession = flowSession))
//    }
//}
