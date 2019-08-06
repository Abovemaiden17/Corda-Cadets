package com.template.Models

import com.fasterxml.jackson.annotation.JsonCreator
import com.r3.corda.lib.tokens.contracts.types.IssuedTokenType
import com.r3.corda.lib.tokens.contracts.types.TokenType
import net.corda.core.contracts.Amount
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.crypto.SecureHash
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party


data class FungibleTokenModel(
        val amount: Amount<IssuedTokenType>,
        val holder: AbstractParty,
        val tokenTypeJarHash: SecureHash?

)

data class HouseStateModel(
        val mayAri: Party,
        val address: String,
        val valuation: Amount<TokenType>,
        val maintainers: List<Party>,
        val fractionDigits: Int = 0,
        val linearId: UniqueIdentifier

)

data class NonFungibleModel(
        val token: IssuedTokenType,
        val holder: AbstractParty,
        val linearId: UniqueIdentifier,
        val tokenTypeJarHash: SecureHash?
)

//data class NonFungibleTokenModel(
//        val amount: Amount<IssuedTokenType>,
//        val holder: AbstractParty,
//        val tokenTypeJarHash: SecureHash?
//)


data class FiatCurrencyIssueModel @JsonCreator constructor(
        val currency: String,
        val amount: Long,
        val recipient: String

)

data class NonFungibleCreateModel @JsonCreator constructor(
        val mayAri: String,
        val amount: Long,
        val currency: String
)

data class NonFungibleIssueModel @JsonCreator constructor(
        val linearId: String,
        val owner: String
)
data class HouseSaleModel @JsonCreator constructor(
        val houseId: String,
        val buyer: String
)

data class TryModel (
      val base: String

)




//data class TokenSettleModel @JsonCreator constructor(
//        val amountToPay: Long,
//        val counterParty: String,
//        val linearId: UniqueIdentifier
//)
//
//data class TokenSelfIssueModel @JsonCreator constructor(
//        val walletBalance: Long,
//        val linearId: UniqueIdentifier
//)

