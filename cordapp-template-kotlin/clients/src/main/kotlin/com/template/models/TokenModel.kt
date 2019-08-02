package com.template.models

import com.fasterxml.jackson.annotation.JsonCreator
import com.r3.corda.lib.tokens.contracts.types.IssuedTokenType
import com.r3.corda.lib.tokens.contracts.types.TokenType
import net.corda.core.contracts.Amount
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.serialization.CordaSerializable
import org.bouncycastle.cms.bc.BcKEKEnvelopedRecipient

@CordaSerializable
data class TokenModel(val token: IssuedTokenType,
                      val holder: AbstractParty,
                      val linearId: UniqueIdentifier,
                      val tokentype :String)

@CordaSerializable
data class Fungibletokens @JsonCreator constructor(val amount: Amount<IssuedTokenType>,
                                                   val holder: AbstractParty,
                                                   val tokentype: String)


data class CreateIssueHouse @JsonCreator constructor(val owner: String,
                                                     val address: String,
                                                     val amount: Long,
                                                     val currency: String)

data class IssueFungible @JsonCreator constructor(val currency: String,
                                                  val amount: Long,
                                                  val recipient: String)

data class SellHouse @JsonCreator constructor(val linearId: String,
                                              val buyer: String)
data class UpdateHouseValuation @JsonCreator constructor(val linearId: String,
                                                         val amount: Long,
                                                         val currency: String)

