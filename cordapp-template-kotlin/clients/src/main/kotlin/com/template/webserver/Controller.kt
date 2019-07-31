package com.template.webserver

import com.fasterxml.jackson.databind.ObjectMapper
import com.r3.corda.lib.tokens.contracts.states.NonFungibleToken
import com.template.models.TokenModel
import com.template.utilities.FlowHandlerCompletion
import net.corda.core.messaging.vaultQueryBy
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


/**
 * Define your API endpoints here.
 */
@RestController
@RequestMapping("/token_sdk") // The paths for HTTP requests are relative to this base path.
class Controller(private val rpc: NodeRPCConnection,val flowSession: FlowHandlerCompletion) {

    private val proxy = rpc.proxy

    @GetMapping("getNonfungibleToken",produces = ["application/json"])
    fun getnonFungibleToken(): ResponseEntity<Map<String,Any>>
    {

//        @Configuration
//        class Plugin {
//
//            @Bean
//            fun registerModule(): ObjectMapper {
//                return JacksonSupport.createNonRpcMapper()
//            }
//        }
        val (status,result) = try
        {
            val nonFungibleStateRef = proxy.vaultQueryBy<NonFungibleToken>().states
            val nonFungibleTokenState =nonFungibleStateRef.map { it.state.data }
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
}