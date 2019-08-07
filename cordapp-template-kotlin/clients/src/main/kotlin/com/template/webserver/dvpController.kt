package com.template.webserver

import com.fasterxml.jackson.databind.SerializationFeature
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.r3.corda.lib.tokens.contracts.states.FungibleToken
import com.r3.corda.lib.tokens.contracts.states.NonFungibleToken
import com.template.DVPstateAndContract.HouseState
import com.template.DVPstateAndContract.trystate
import com.template.Models.*
import com.template.flows.DVP.FiatCurrencyIssueFlow
import com.template.flows.DVP.HouseSaleFlow
import com.template.flows.DVP.HouseTokenCreateFlow
import com.template.flows.DVP.HouseTokenIssueFlow
import com.template.webserver.utilities.FlowHandlerCompletion
import com.template.webserver.utilities.Plugin
import net.corda.client.jackson.JacksonSupport
import net.corda.core.messaging.vaultQueryBy
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClientBuilder
import org.springframework.http.HttpRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.io.BufferedReader
import java.io.InputStreamReader



@CrossOrigin
@RestController
@RequestMapping("/token") // The paths for HTTP requests are relative to this base path.
class dvpController(rpc: NodeRPCConnection, val flowHandlerCompletion: FlowHandlerCompletion ,val plugin: Plugin){

    companion object {
        private val logger = org.slf4j.LoggerFactory.getLogger(RestController::class.java)
    }

    private val proxy = rpc.proxy

//  @GetMapping(value = "/templateendpoint", produces = arrayOf("text/plain"))
//    private fun templateendpoint(): String {
//        return "Define an endpoint here."
//    }

    @PostMapping(value = "/fungibletoken/issue", produces = arrayOf("application/json"))

    private fun TokenIssueModel(@RequestBody TokenIssue: FiatCurrencyIssueModel): ResponseEntity<Map<String, Any>> {
        val (status, result) = try {
            val TokenIssued = FiatCurrencyIssueModel(currency = TokenIssue.currency, amount = TokenIssue.amount, recipient = TokenIssue.recipient)

            val flowReturn = proxy.startFlowDynamic(

                    FiatCurrencyIssueFlow::class.java,
                    TokenIssued.currency,
                    TokenIssued.amount,
                    TokenIssued.recipient
            )

            flowHandlerCompletion.flowHandlerCompletion(flowReturn)
            HttpStatus.CREATED to TokenIssue

        } catch (e: Exception) {
            HttpStatus.BAD_REQUEST to "$e"
        }
        val stat = "status" to status.value()

        val mess = if (status == HttpStatus.CREATED) {
            "message" to "Successful"
        } else {
            "message" to "Failed"
        }
        val res = "result" to result
        return ResponseEntity.status(status).body(mapOf(stat, mess, res))
    }

    @GetMapping(value = "/fungibletoken", produces = arrayOf("application/json"))
    private fun getFungibleToken(): ResponseEntity<Map<String, Any>> {

        plugin.registerModule().configure(SerializationFeature.FAIL_ON_EMPTY_BEANS,false)
        val (status, result) = try {
            val TokenStateRef = proxy.vaultQueryBy<FungibleToken>().states
            val tokenState = TokenStateRef.map { it.state.data }
            val list = tokenState.map {

                FungibleTokenModel(it.amount,
                        it.holder,
                        it.tokenTypeJarHash)
            }

            HttpStatus.CREATED to list
        } catch (e: Exception) {
            HttpStatus.BAD_REQUEST to "No data"
        }
        val stat = "status" to status

        val mess = if (status == HttpStatus.CREATED) {
            "message" to "Successful"
        } else {
            "message" to "Failed"
        }
        val res = "result" to result

        return ResponseEntity.status(status).body(mapOf(stat, mess, res))

    }


    @PostMapping(value = "/nonfungibletoken/create", produces = arrayOf("application/json"))

    private fun NonFungibleCreateModel(@RequestBody NonFungibleCreate: NonFungibleCreateModel): ResponseEntity<Map<String, Any>> {
        val (status, result) = try {
            val NonFungibleCreateToken = NonFungibleCreateModel(NonFungibleCreate.mayAri, NonFungibleCreate.amount, NonFungibleCreate.currency)

            val flowReturn = proxy.startFlowDynamic(

                    HouseTokenCreateFlow::class.java,
                    NonFungibleCreateToken.mayAri,
                    NonFungibleCreateToken.amount,
                    NonFungibleCreateToken.currency
            )

            flowHandlerCompletion.flowHandlerCompletion(flowReturn)
            HttpStatus.CREATED to NonFungibleCreate

        } catch (e: Exception) {
            HttpStatus.BAD_REQUEST to "$e"
        }
        val stat = "status" to status.value()

        val mess = if (status == HttpStatus.CREATED) {
            "message" to "Successful"
        } else {
            "message" to "Failed"
        }
        val res = "result" to result
        return ResponseEntity.status(status).body(mapOf(stat, mess, res))
    }


    @PostMapping(value = "/nonfungibletoken/issue", produces = arrayOf("application/json"))

    private fun NonFungibleIssueModel(@RequestBody NonFungibleIssue: NonFungibleIssueModel): ResponseEntity<Map<String, Any>> {
        val (status, result) = try {
            val NonFungibleIssueToken = NonFungibleIssueModel(NonFungibleIssue.linearId, NonFungibleIssue.owner)

            val flowReturn = proxy.startFlowDynamic(

                    HouseTokenIssueFlow::class.java,
                    NonFungibleIssueToken.linearId,
                    NonFungibleIssueToken.owner

            )

            flowHandlerCompletion.flowHandlerCompletion(flowReturn)
            HttpStatus.CREATED to NonFungibleIssue

        } catch (e: Exception) {
            HttpStatus.BAD_REQUEST to "$e"
        }
        val stat = "status" to status.value()

        val mess = if (status == HttpStatus.CREATED) {
            "message" to "Successful"
        } else {
            "message" to "Failed"
        }
        val res = "result" to result
        return ResponseEntity.status(status).body(mapOf(stat, mess, res))
    }

    @GetMapping(value = "/housestate", produces = arrayOf("application/json"))
    private fun getHouseState(): ResponseEntity<Map<String, Any>> {
        val (status, result) = try {
            val TokenStateRef = proxy.vaultQueryBy<HouseState>().states
            val tokenState = TokenStateRef.map { it.state.data }
            val list = tokenState.map {

                HouseStateModel(
                        it.mayAri,
                        it.address,
                        it.valuation,
                        it.maintainers,
                        it.fractionDigits,
                        it.linearId
                )
            }
            HttpStatus.CREATED to list
        } catch (e: Exception) {
            HttpStatus.BAD_REQUEST to "No data"
        }
        val stat = "status" to status

        val mess = if (status == HttpStatus.CREATED) {
            "message" to "Successful"
        } else {
            "message" to "Failed"
        }
        val res = "result" to result

        return ResponseEntity.status(status).body(mapOf(stat, mess, res))

    }

    @GetMapping(value = "/nonfungible", produces = arrayOf("application/json"))
    private fun getNonFungibleToken(): ResponseEntity<Map<String, Any>> {
        plugin.registerModule().configure(SerializationFeature.FAIL_ON_EMPTY_BEANS,false)
        val (status, result) = try {
            val TokenStateRef = proxy.vaultQueryBy<NonFungibleToken>().states
            val tokenState = TokenStateRef.map { it.state.data }
            val list = tokenState.map {

                NonFungibleModel(
                        it.token,
                        it.holder,
                        it.linearId,
                        it.tokenTypeJarHash
                )
            }
            HttpStatus.CREATED to list
        } catch (e: Exception) {
            HttpStatus.BAD_REQUEST to "No data"
        }
        val stat = "status" to status

        val mess = if (status == HttpStatus.CREATED) {
            "message" to "Successful"
        } else {
            "message" to "Failed"
        }
        val res = "result" to result

        return ResponseEntity.status(status).body(mapOf(stat, mess, res))

    }


    @PostMapping(value = "/sellhouse", produces = arrayOf("application/json"))

    private fun HouseSaleModel(@RequestBody SellHouse: HouseSaleModel): ResponseEntity<Map<String, Any>> {
        val (status, result) = try {
            val SellTheHouse = HouseSaleModel(SellHouse.houseId,SellHouse.buyer)

            val flowReturn = proxy.startFlowDynamic(

                    HouseSaleFlow::class.java,
                    SellTheHouse.houseId,
                    SellTheHouse.buyer
            )

            flowHandlerCompletion.flowHandlerCompletion(flowReturn)
            HttpStatus.CREATED to SellHouse

        } catch (e: Exception) {
            HttpStatus.BAD_REQUEST to "$e"
        }
        val stat = "status" to status.value()

        val mess = if (status == HttpStatus.CREATED) {
            "message" to "Successful"
        } else {
            "message" to "Failed"
        }
        val res = "result" to result
        return ResponseEntity.status(status).body(mapOf(stat, mess, res))
    }

//
    @GetMapping(value = "/trysample", produces = arrayOf("application/json"))
    private fun trysample() : ResponseEntity<Map<String, Any>> {
    val httpclient = HttpClientBuilder.create().build()
    val request = HttpGet("https://api.exchangeratesapi.io/latest?base=USD&symbols=PHP,USD")
    val response = httpclient.execute(request)
    val inputStreamReader = InputStreamReader(response.entity.content)
        val (status, result) = try {
            val TokenStateRef = proxy.vaultQueryBy<trystate>().states
            val tokenState = TokenStateRef.map { it.state.data }
            val list = tokenState.map {

                val bufferReader = BufferedReader(inputStreamReader).use {
                    val stringBuff = StringBuffer()
                    var inputLine = it.readLine()
                    while (inputLine != null) {
                        stringBuff.append(inputLine)
                        inputLine = it.readLine()
                    }
                    val gson = GsonBuilder().create()
                    val jsonWholeObject = gson.fromJson<JsonObject>(stringBuff.toString(), JsonObject::class.java)

                    val basee = jsonWholeObject.get("base")
                    val ratess = jsonWholeObject.get("rates")
                    val dates = jsonWholeObject.get("date")

                    TryModel(
                            rates = ratess.toString(),
                            base = basee.toString(),
                            date = dates.toString()
                    )
                }
            }
            HttpStatus.CREATED to list
        } catch (e: Exception) {
            HttpStatus.BAD_REQUEST to "No data"
        }
        val stat = "status" to status

        val mess = if (status == HttpStatus.CREATED) {
            "message" to "Successful"
        } else {
            "message" to "Failed"
        }
        val res = "result" to result


        return ResponseEntity.status(status).body(mapOf(stat, mess, res))


}

}