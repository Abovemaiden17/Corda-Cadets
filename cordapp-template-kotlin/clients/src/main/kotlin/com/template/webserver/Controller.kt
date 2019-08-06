package com.template.webserver

import com.fasterxml.jackson.databind.SerializationFeature
import com.r3.corda.lib.tokens.contracts.states.FungibleToken
import com.r3.corda.lib.tokens.contracts.states.NonFungibleToken
import com.template.flows.tokenSdkFlow.FiatCurrencyIssueFlow
import com.template.flows.tokenSdkFlow.HouseSaleInitiatorFlow
import com.template.flows.tokenSdkFlow.HouseTokenCreateAndIssueFlow
//import com.sun.xml.internal.ws.developer.SerializationFeature
import com.template.tokenSdk.HouseState
import net.corda.core.messaging.vaultQueryBy
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

private const val CONTROLLER_NAME = "config.controller.name"
//@Value("\${$CONTROLLER_NAME}") private val controllerName: String

@CrossOrigin
@RestController
@RequestMapping("/") // The paths for HTTP requests are relative to this base path.
class UserAccountController(
        private val rpc: NodeRPCConnection,
        private val flowHandlerCompletion : FlowHandlerCompletion<Any?>,
        private val plugin: Plugin

) {
    companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }

    private val proxy = rpc.proxy


    /**
     * Return all UserAccountState
     */
    @GetMapping(value = ["/vault"], produces = ["application/json"])
    private fun getUserVault(): ResponseEntity<Map<String, Any>> {
        val (status, result) = try {
            val requestStateRef = rpc.proxy.vaultQueryBy<HouseState>().states
            val requestStates = requestStateRef.map { it.state.data }
            val list = requestStates.map {
                TokenSDKModel(
                        valuation = it.valuation,
                        maintainers = listOf(it.maintainers.toString()),
                        linearId = it.linearId
                )
            }
            HttpStatus.CREATED to list
        } catch (e: Exception) {
            HttpStatus.BAD_REQUEST to "No data"
        }
        val stat = "status" to status
        val mess = if (status == HttpStatus.CREATED) {
            "message" to "Successful in getting ContractState of type UserAccountState"
        } else {
            "message" to "Failed to get ContractState of type UserAccountState"
        }
        val res = "result" to result
        return ResponseEntity.status(status).body(mapOf(stat, mess, res))
    }

    /**
     * NonFungible
     */
    @GetMapping(value = ["/nonFungible"], produces = ["application/json"])
    private fun getUserNonFungible(): ResponseEntity<Map<String, Any>> {
        plugin.registerModule().configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
        val (status, result) = try {
            val requestStateRef = rpc.proxy.vaultQueryBy<NonFungibleToken>().states
            val requestStates = requestStateRef.map { it.state.data }
            val list = requestStates.map {
                NonFungibleModel(
                        token = it.token,
                        holder = it.holder.toString(),
                        linearId = it.linearId,
                        issuer = it.issuer.toString()
                )
            }
            HttpStatus.CREATED to list
        } catch (e: Exception) {
            HttpStatus.BAD_REQUEST to "No data"
        }
        val stat = "status" to status
        val mess = if (status == HttpStatus.CREATED) {
            "message" to "Successful in getting ContractState of type UserAccountState"
        } else {
            "message" to "Failed to get ContractState of type UserAccountState"
        }
        val res = "result" to result
        return ResponseEntity.status(status).body(mapOf(stat, mess, res))
    }


    /**
     * Fungible
     */
    @GetMapping(value = ["/fungible"], produces = ["application/json"])
    private fun getUserFungible(): ResponseEntity<Map<String, Any>> {
        plugin.registerModule().configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
        val (status, result) = try {
            val requestStateRef = rpc.proxy.vaultQueryBy<FungibleToken>().states
            val requestStates = requestStateRef.map { it.state.data }
            val list = requestStates.map {
                FungibleModel(
                        amount = it.amount,
                        holder = it.holder.toString(),
                        issuer = it.issuer.toString()
                )
            }
            HttpStatus.CREATED to list
        } catch (e: Exception) {
            HttpStatus.BAD_REQUEST to "No data"
        }
        val stat = "status" to status
        val mess = if (status == HttpStatus.CREATED) {
            "message" to "Successful in getting ContractState of type UserAccountState"
        } else {
            "message" to "Failed to get ContractState of type UserAccountState"
        }
        val res = "result" to result
        return ResponseEntity.status(status).body(mapOf(stat, mess, res))
    }

    /**
     * CreateAndIssue
     */

    @PostMapping(value = ["/createIssue"], produces = ["application/json"])
    private fun createIssueModel(@RequestBody createIssueModel: CreateIssueModel) : ResponseEntity<Map<String,Any>> {

        val (status, result) = try {
            val user = CreateIssueModel(
                    amount = createIssueModel.amount,
                    currency = createIssueModel.currency,
                    owner = createIssueModel.owner

            )
            val flowReturn= proxy.startFlowDynamic(
                    HouseTokenCreateAndIssueFlow::class.java,
                    user.amount,
                    user.currency,
                    user.owner
            )


            flowHandlerCompletion.flowHandlerCompletion(flowReturn)
            HttpStatus.CREATED to createIssueModel
        }catch (e: Exception){
            HttpStatus.BAD_REQUEST to "No data"
        }
        val stat = "status" to status
        val mess = if (status==HttpStatus.CREATED){
            "message" to "Successful in creating ContractState of type UserAccountState"}
        else{ "message" to "Failed to create ContractState of type UserAccountState"}
        val res = "result" to result
        return ResponseEntity.status(status).body(mapOf(stat,mess,res))
    }



    /**
     * FiatCurrencyIssueFlow
     */

    @PostMapping(value = ["/fiatIssue"], produces = ["application/json"])
    private fun fiatCurrencyIssueModel(@RequestBody fiatCurrencyIssueModel: FiatCurrencyIssueModel) : ResponseEntity<Map<String,Any>> {

        val (status, result) = try {
            val user = FiatCurrencyIssueModel(
                    currency = fiatCurrencyIssueModel.currency,
                    amount = fiatCurrencyIssueModel.amount,
                    recipient = fiatCurrencyIssueModel.recipient

            )
            val flowReturn= proxy.startFlowDynamic(
                    FiatCurrencyIssueFlow::class.java,
                    user.currency,
                    user.amount,
                    user.recipient

            )


            flowHandlerCompletion.flowHandlerCompletion(flowReturn)
            HttpStatus.CREATED to fiatCurrencyIssueModel
        }catch (e: Exception){
            HttpStatus.BAD_REQUEST to "No data"
        }
        val stat = "status" to status
        val mess = if (status==HttpStatus.CREATED){
            "message" to "Successful in creating ContractState of type UserAccountState"}
        else{ "message" to "Failed to create ContractState of type UserAccountState"}
        val res = "result" to result
        return ResponseEntity.status(status).body(mapOf(stat,mess,res))
    }

    /**
     * FiatCurrencyIssueFlow
     */

    @PostMapping(value = ["/houseSale"], produces = ["application/json"])
    private fun houseSaleInitiatorModel(@RequestBody houseSaleInitiatorModel : HouseSaleInitiatorModel) : ResponseEntity<Map<String,Any>> {

        val (status, result) = try {
            val user = HouseSaleInitiatorModel(
                    linearId = houseSaleInitiatorModel.linearId,
                    buyer = houseSaleInitiatorModel.buyer

            )
            val flowReturn= proxy.startFlowDynamic(
                    HouseSaleInitiatorFlow::class.java,
                    user.linearId,
                    user.buyer

            )


            flowHandlerCompletion.flowHandlerCompletion(flowReturn)
            HttpStatus.CREATED to houseSaleInitiatorModel
        }catch (e: Exception){
            HttpStatus.BAD_REQUEST to "No data"
        }
        val stat = "status" to status
        val mess = if (status==HttpStatus.CREATED){
            "message" to "Successful in creating ContractState of type UserAccountState"}
        else{ "message" to "Failed to create ContractState of type UserAccountState"}
        val res = "result" to result
        return ResponseEntity.status(status).body(mapOf(stat,mess,res))
    }




}