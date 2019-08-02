package com.template.webserver

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.r3.corda.lib.tokens.contracts.states.FungibleToken
import com.r3.corda.lib.tokens.contracts.states.NonFungibleToken
import com.template.flows.nonfungibletokentest.*
import com.template.models.*
import com.template.utilities.FlowHandlerCompletion
import com.template.utilities.Plugin
import net.corda.client.jackson.JacksonSupport
import net.corda.core.contracts.Amount
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.serialization.serialize
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


/**
 * Define your API endpoints here.
 */
@RestController
@RequestMapping("/token_sdk") // The paths for HTTP requests are relative to this base path.
class Controller(private val rpc: NodeRPCConnection,val flowSession: FlowHandlerCompletion,val plugin: Plugin) {

    private val proxy = rpc.proxy

    @GetMapping("getNonfungibleTokens",produces = ["application/json"])
    fun getnonFungibleToken(): ResponseEntity<Map<String,Any>>
    {
        plugin.registerModule().configure(SerializationFeature.FAIL_ON_EMPTY_BEANS,false)
        val (status,result) = try
        {
            val nonFungibleStateRef = proxy.vaultQueryBy<NonFungibleToken>().states
            val nonFungibleTokenState = nonFungibleStateRef.map { it.state.data }
            val list = nonFungibleTokenState.map {
                TokenModel(token = it.token,holder = it.holder,linearId = it.linearId,tokentype = it.tokenType.toString())
            }
            HttpStatus.CREATED to list

        }catch (e:Exception)
        {
            HttpStatus.BAD_REQUEST to "No data"
        }
        val stat = "status" to status
        val mess = if (status==HttpStatus.CREATED){
            "message" to "Successful in getting ContractState of type UserState"}
        else{ "message" to "Failed to get ContractState of type UserState"}
        val res = "result" to result
        return ResponseEntity.status(status).body(mapOf(stat,mess,res))
    }
    @GetMapping("getFungibleTokens",produces = ["application/json"])
    fun getfungibleToken(): ResponseEntity<Map<String,Any>>
    {
        plugin.registerModule().configure(SerializationFeature.FAIL_ON_EMPTY_BEANS,false)
        val (status,result) = try {
            val fungibleToken = proxy.vaultQueryBy<FungibleToken>().states
            val fungibleTokenState = fungibleToken.map { it.state.data }
            val list = fungibleTokenState.map {
                Fungibletokens(amount = it.amount,holder = it.holder,tokentype = it.tokenType.toString())
            }
            HttpStatus.CREATED to list
        }
        catch (e:Exception)
        {
            HttpStatus.BAD_REQUEST to "No data"
        }
        val stat = "status" to status
        val mess = if (status==HttpStatus.CREATED){
            "message" to "Successful in getting ContractState of type UserState"}
        else{ "message" to "Failed to get ContractState of type UserState"}
        val res = "result" to result
        return ResponseEntity.status(status).body(mapOf(stat,mess,res))
    }
    @GetMapping("getHouseState",produces = ["application/json"])
    fun gethouseState(): ResponseEntity<Map<String,Any>>
    {
        val (status,result) = try {
            val houseState = proxy.vaultQueryBy<TokenHouseState>().states
            val getState = houseState.map { it.state.data }
            val list = getState.map {
                TokenHouseState(owner = it.owner,
                                address = it.address,
                                valuation = it.valuation,
                                fractionDigits = it.fractionDigits,
                                maintainers = it.maintainers,
                                linearId = it.linearId)
            }
            HttpStatus.CREATED to list
        }
        catch (e:Exception)
        {
            HttpStatus.BAD_REQUEST to "No data"
        }
        val stat = "status" to status
        val mess = if (status==HttpStatus.CREATED){
            "message" to "Successful in getting ContractState of type UserState"}
        else{ "message" to "Failed to get ContractState of type UserState"}
        val res = "result" to result
        return ResponseEntity.status(status).body(mapOf(stat,mess,res))
    }
    @PostMapping("createIssueHouse",produces = ["application/json"])
    fun createissueHouse(@RequestBody createHouse: CreateIssueHouse): ResponseEntity<Map<String,Any>>
    {
        val (status,result) = try {
            val createissuehouse = CreateIssueHouse(owner = createHouse.owner,
                                                    address = createHouse.address,
                                                    amount = createHouse.amount,
                                                    currency = createHouse.currency)
            val flowReturn = proxy.startFlowDynamic(
                    CreateHouseFlow::class.java,
                    createissuehouse.owner,
                    createissuehouse.address,
                    createissuehouse.amount,
                    createissuehouse.currency
            )
            flowSession.flowHandlerCompletion(flowReturn)
            HttpStatus.CREATED to createHouse
        }
        catch (e: Exception)
        {
            HttpStatus.BAD_REQUEST to e
        }
        val stat = "status" to status
        val mess = if (status == HttpStatus.CREATED)
        {
            "mesasge" to "Successful"
        }
        else
        {
            "message" to "Failed"
        }

        val res = "result" to result
        return ResponseEntity.status(status).body(mapOf(stat, mess, res))
    }
    @PostMapping("issueFungibleToken",produces = ["application/json"])
    fun issuefungibleToken(@RequestBody issueFungibles: IssueFungible): ResponseEntity<Map<String,Any>>
    {
        val (status,result) = try
        {
            val issuetokens = IssueFungible(currency = issueFungibles.currency,
                                            amount = issueFungibles.amount,
                                            recipient = issueFungibles.recipient)
            val flowReturn = proxy.startFlowDynamic(
                    FiatCurrencyIssueFlow::class.java,
                    issuetokens.currency,
                    issuetokens.amount,
                    issuetokens.recipient
            )
            flowSession.flowHandlerCompletion(flowReturn)
            HttpStatus.CREATED to issueFungibles
        }
        catch (e: Exception)
        {
            HttpStatus.BAD_REQUEST to e
        }
        val stat = "status" to status
        val mess = if (status == HttpStatus.CREATED)
        {
            "mesasge" to "Successful"
        }
        else
        {
            "message" to "Failed"
        }

        val res = "result" to result
        return ResponseEntity.status(status).body(mapOf(stat, mess, res))
    }
    @PostMapping("sellHouse",produces = ["application/json"])
    fun sellhouse(@RequestBody sellhouseState: SellHouse): ResponseEntity<Map<String,Any>>
    {
        val (status,result) = try {
            val sell = SellHouse(linearId = sellhouseState.linearId,buyer = sellhouseState.buyer)
            val flowReturn = proxy.startFlowDynamic(
                    HouseSaleFlow::class.java,
                    sell.linearId,
                    sell.buyer
            )
            flowSession.flowHandlerCompletion(flowReturn)
            HttpStatus.CREATED to sellhouseState
        }
        catch (e: Exception)
        {
            HttpStatus.BAD_REQUEST to e
        }
        val stat = "status" to status
        val mess = if (status == HttpStatus.CREATED)
        {
            "mesasge" to "Successful"
        }
        else
        {
            "message" to "Failed"
        }

        val res = "result" to result
        return ResponseEntity.status(status).body(mapOf(stat, mess, res))
    }
    @PutMapping("updateValuation",produces = ["application/json"])
    fun updateValuation(@RequestBody updatehousevaluation: UpdateHouseValuation): ResponseEntity<Map<String,Any>>
    {
        val (status, result) = try {
            val updateHousevalue = UpdateHouseValuation(linearId = updatehousevaluation.linearId,
                                                        amount = updatehousevaluation.amount,
                                                        currency = updatehousevaluation.currency)
            val flowReturn = proxy.startFlowDynamic(
                    UpdateHouseValuationFlow::class.java,
                    updateHousevalue.linearId,
                    updateHousevalue.amount,
                    updateHousevalue.currency

            )
            flowSession.flowHandlerCompletion(flowReturn)
            HttpStatus.CREATED to updateHousevalue
        }
        catch (e: Exception)
        {
            HttpStatus.BAD_REQUEST to e
        }
        val stat = "status" to status
        val mess = if (status == HttpStatus.CREATED)
        {
            "mesasge" to "Successful"
        }
        else
        {
            "message" to "Failed"
        }

        val res = "result" to result
        return ResponseEntity.status(status).body(mapOf(stat, mess, res))
    }
}