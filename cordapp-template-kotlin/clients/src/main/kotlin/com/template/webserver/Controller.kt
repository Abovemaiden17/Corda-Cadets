package com.template.webserver

import com.fasterxml.jackson.databind.SerializationFeature
import com.r3.corda.lib.tokens.contracts.states.FungibleToken
import com.r3.corda.lib.tokens.contracts.states.NonFungibleToken
import com.template.flows.houseDVP.FiatCurrencyIssueFlow
import com.template.flows.houseDVP.HouseSaleFlow
import com.template.flows.houseDVP.HouseTokenCreateAndIssueFlow
import com.template.flows.houseDVP.UpdateHouseValuationFlow
import com.template.houseDVP.HouseDVPState
import com.template.models.*
import com.template.webserver.utilities.Plugin
import net.corda.core.messaging.vaultQueryBy
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * Define your API endpoints here.
 */
private const val CONTROLLER_NAME = "config.controller.name"
@RestController
@RequestMapping("/") // The paths for HTTP requests are relative to this base path.
class Controller(private val rpc: NodeRPCConnection, private val plugin: Plugin) {

    companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }

    private val proxy = rpc.proxy


    /**
     *      House DVP State
     */
    @GetMapping(value = ["/states/houseDVP"], produces = ["application/json"])
    private fun getHouseDVPStates(): ResponseEntity<Map<String,Any>> {
        plugin.registerModule().configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
        val (status, result) = try {
            val houseDVPStateRef = rpc.proxy.vaultQueryBy<HouseDVPState>().states
            val requestStates = houseDVPStateRef.map { it.state.data }
            val list = requestStates.map {
                HouseDVPModel (
                        owner = it.owner.toString(),
                        fractionDigits = it.fractionDigits,
                        linearId = it.linearId,
                        maintainers = it.maintainers.toString(),
                        valuation = it.valuation,
                        noOfBedRooms = it.noOfBedRooms,
                        address = it.address
                )
            }
            HttpStatus.CREATED to list
        } catch (e: Exception) {
            HttpStatus.BAD_REQUEST to "No data"
        }
        val stat = "status" to status
        val mess = if (status == HttpStatus.CREATED) {
            "message" to "Successful!"
        } else {
            "message" to "Failed!"
        }
        val res = "result" to result
        return ResponseEntity.status(status).body(mapOf(stat, mess, res))
    }

    /**
     *      Fungible Token State
     */
    @GetMapping(value = ["/states/fungibleToken"], produces = ["application/json"])
    private fun getFungibleTokenStates(): ResponseEntity<Map<String,Any>> {
        plugin.registerModule().configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
        val (status, result) = try {
            val fungibleTokenStateRef = rpc.proxy.vaultQueryBy<FungibleToken>().states
            val requestStates = fungibleTokenStateRef.map { it.state.data }
            val list = requestStates.map {
                FungibleTokenModel (
                        amount = it.amount,
                        holder = it.holder,
                        tokenTypeJarHash = it.tokenTypeJarHash
                )
            }
            HttpStatus.CREATED to list
        } catch (e: Exception) {
            HttpStatus.BAD_REQUEST to "No data"
        }
        val stat = "status" to status
        val mess = if (status == HttpStatus.CREATED) {
            "message" to "Successful!"
        } else {
            "message" to "Failed!"
        }
        val res = "result" to result
        return ResponseEntity.status(status).body(mapOf(stat, mess, res))
    }


    /**
     *     Non-Fungible Token State
     */
    @GetMapping(value = ["/states/nonFungibleToken"], produces = ["application/json"])
    private fun getNonFungibleTokenStates(): ResponseEntity<Map<String,Any>> {
        plugin.registerModule().configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
        val (status, result) = try {
            val nonFungibleTokenStateRef = rpc.proxy.vaultQueryBy<NonFungibleToken>().states
            val requestStates = nonFungibleTokenStateRef.map { it.state.data }
            val list = requestStates.map {
                NonFungibleTokenModel (
                        token = it.token,
                        holder = it.holder,
                        linearId = it.linearId,
                        tokenTypeJarHash = it.tokenTypeJarHash
                )
            }
            HttpStatus.CREATED to list
        } catch (e: Exception) {
            HttpStatus.BAD_REQUEST to "No data"
        }
        val stat = "status" to status
        val mess = if (status == HttpStatus.CREATED) {
            "message" to "Successful!"
        } else {
            "message" to "Failed!"
        }
        val res = "result" to result
        return ResponseEntity.status(status).body(mapOf(stat, mess, res))
    }


    /**
     *      FiatCurrency Issue flow
     */
    @PostMapping (value = ["/flows/fiatCurrencyIssueFlow"], produces = ["application/json"])
    private fun fiatCurrencyIssue(@RequestBody fiatCurrencyIssue: FiatCurrencyIssueModel): ResponseEntity<Map<String,Any>> {
        val (status, result) = try {
            val fiatCurrency = FiatCurrencyIssueModel(
                    currency = fiatCurrencyIssue.currency,
                    amount = fiatCurrencyIssue.amount,
                    recipient = fiatCurrencyIssue.recipient
            )
            proxy.startFlowDynamic(FiatCurrencyIssueFlow::class.java,
                    fiatCurrency.currency,
                    fiatCurrency.amount,
                    fiatCurrency.recipient)

            HttpStatus.CREATED to fiatCurrencyIssue
        } catch (e: Exception) {
            HttpStatus.BAD_REQUEST to "No data"
        }
        val stat = "status" to status
        val mess = if (status == HttpStatus.CREATED) {
            "message" to "Successful!"
        } else {
            "message" to "Failed"
        }
        val res = "result" to result
        return ResponseEntity.status(status).body(mapOf(stat, mess, res))
    }


    /**
     *      House Token Create and Issue Flow
     */
    @PostMapping (value = ["/flows/houseTokenCreateAndIssue"], produces = ["application/json"])
    private fun houseTokenCreateAndIssue(@RequestBody createAndIssueHouse: CreateAndIssueHouseModel): ResponseEntity<Map<String,Any>> {
        val (status, result) = try {
            val createAndIssue = CreateAndIssueHouseModel(
                    owner = createAndIssueHouse.owner,
                    amount = createAndIssueHouse.amount,
                    currency = createAndIssueHouse.currency,
                    noOfBedRooms = createAndIssueHouse.noOfBedRooms,
                    address = createAndIssueHouse.address
            )
            proxy.startFlowDynamic(HouseTokenCreateAndIssueFlow::class.java,
                    createAndIssue.owner,
                    createAndIssue.amount,
                    createAndIssue.currency,
                    createAndIssue.noOfBedRooms,
                    createAndIssue.address)

            HttpStatus.CREATED to createAndIssueHouse
        } catch (e: Exception) {
            HttpStatus.BAD_REQUEST to "No data"
        }
        val stat = "status" to status
        val mess = if (status == HttpStatus.CREATED) {
            "message" to "Successful!"
        } else {
            "message" to "Failed"
        }
        val res = "result" to result
        return ResponseEntity.status(status).body(mapOf(stat, mess, res))
    }


    /**
     *      House Sale Flow
     */
    @PostMapping (value = ["/flows/houseSaleFlow"], produces = ["application/json"])
    private fun houseSaleFlow(@RequestBody houseSaleModel: HouseSaleModel): ResponseEntity<Map<String,Any>> {
        val (status, result) = try {
            val houseSale = HouseSaleModel(
                    houseId = houseSaleModel.houseId,
                    buyer = houseSaleModel.buyer
            )
            proxy.startFlowDynamic(HouseSaleFlow::class.java,
                    houseSale.houseId,
                    houseSale.buyer)

            HttpStatus.CREATED to houseSaleModel
        } catch (e: Exception) {
            HttpStatus.BAD_REQUEST to "No data"
        }
        val stat = "status" to status
        val mess = if (status == HttpStatus.CREATED) {
            "message" to "Successful!"
        } else {
            "message" to "Failed"
        }
        val res = "result" to result
        return ResponseEntity.status(status).body(mapOf(stat, mess, res))
    }


    /**
     *      Update House Valuation Flow
     */
    @PostMapping (value = ["/flows/updateHouseValuation"], produces = ["application/json"])
    private fun updateHouseValuationFlow(@RequestBody updateHouseValuationModel: UpdateHouseValuationModel): ResponseEntity<Map<String,Any>> {
        val (status, result) = try {
            val updateValuation = UpdateHouseValuationModel(
                    houseId = updateHouseValuationModel.houseId,
                    newAmount = updateHouseValuationModel.newAmount,
                    newCurrency = updateHouseValuationModel.newCurrency
            )
            proxy.startFlowDynamic(UpdateHouseValuationFlow::class.java,
                    updateValuation.houseId,
                    updateValuation.newAmount,
                    updateValuation.newCurrency)

            HttpStatus.CREATED to updateHouseValuationModel
        } catch (e: Exception) {
            HttpStatus.BAD_REQUEST to "No data"
        }
        val stat = "status" to status
        val mess = if (status == HttpStatus.CREATED) {
            "message" to "Successful!"
        } else {
            "message" to "Failed"
        }
        val res = "result" to result
        return ResponseEntity.status(status).body(mapOf(stat, mess, res))
    }

}