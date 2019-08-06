package com.template.webserver

import com.fasterxml.jackson.annotation.JsonCreator
import com.r3.corda.lib.tokens.contracts.types.IssuedTokenType
import com.r3.corda.lib.tokens.contracts.types.TokenType
import net.corda.core.contracts.Amount
import net.corda.core.contracts.UniqueIdentifier

data class TokenSDKModel(
        val valuation: Amount<TokenType>,
        val maintainers: List<String>,
        val linearId: UniqueIdentifier
)

data class NonFungibleModel(
        val token: IssuedTokenType,
        val holder: String,
        val linearId: UniqueIdentifier,
        val issuer: String
)

data class FungibleModel(
        val amount: Amount<IssuedTokenType>,
        val holder: String,
        val issuer: String

)

data class CreateIssueModel @JsonCreator constructor(
        val amount: Long,
        val currency: String,
        val owner: String
)

data class FiatCurrencyIssueModel @JsonCreator constructor(
        val currency: String,
        val amount: Long,
        val recipient: String
)
data class HouseSaleInitiatorModel @JsonCreator constructor(
        val linearId: String,
        val buyer: String
)

