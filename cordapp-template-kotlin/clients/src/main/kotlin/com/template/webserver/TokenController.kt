package com.template.webserver

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.r3.corda.lib.tokens.contracts.states.FungibleToken
import com.template.models.FungibleTokenModel
import com.template.webserver.utlities.FlowHandlerCompletion
import com.template.webserver.utlities.Plugin
import net.corda.core.crypto.SecureHash
import net.corda.core.messaging.vaultQueryBy
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import quantumtoken.states.FungibleTokenState

@RestController
@RequestMapping("token")
class TokenController (rpc: NodeRPCConnection, private val flowHandlerCompletion: FlowHandlerCompletion, private val plugin: Plugin)
{
    companion object
    {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }

    private val proxy = rpc.proxy

    /**
     * Return all fungible states
     */
    @GetMapping(value = ["states/all"], produces = ["application/json"])
    fun getFungibleStates(): ResponseEntity<Map<String, Any>>
    {
        plugin.registerModule().configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
        val (status, result) = try {
            val infoStateRef = proxy.vaultQueryBy<FungibleTokenState>().states
            val infoStates = infoStateRef.map { it.state.data }
            val list = infoStates.map {
                FungibleTokenModel(
                        amount = it.amount,
                        holder = it.holder,
                        tokenTypeJarHash = it.tokenTypeJarHash
                )
            }
            HttpStatus.CREATED to list
        }
        catch (e: Exception)
        {
            logger.info(e.message)
            HttpStatus.BAD_REQUEST to "No fungible state found."
        }
        val stat = "status" to status
        val mess = if (status == HttpStatus.CREATED)
        {
            "message" to "Successful"
        }
        else
        {
            "message" to "Failed"
        }

        val res = "result" to result
        return ResponseEntity.status(status).body(mapOf(stat,mess,res))
    }

    /**
     * Issue a Fungible Token
     */
}