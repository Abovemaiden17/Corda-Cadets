package com.template.models

import com.fasterxml.jackson.annotation.JsonCreator
import com.r3.corda.lib.tokens.contracts.types.IssuedTokenType
import com.r3.corda.lib.tokens.contracts.types.TokenType
import net.corda.core.contracts.Amount
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.crypto.SecureHash
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import java.util.*

data class HouseDVPModel (
        val owner: String,
        val fractionDigits: Int,
        val linearId: UniqueIdentifier,
        val maintainers: String,
        val valuation: Amount<TokenType>,
        val noOfBedRooms: Int,
        val address: String
)

data class FungibleTokenModel (
        val amount: Amount<IssuedTokenType>,
        val holder: AbstractParty,
        val tokenTypeJarHash: SecureHash?

)

data class NonFungibleTokenModel (
        val token: IssuedTokenType,
        val holder: AbstractParty,
        val linearId: UniqueIdentifier,
        val tokenTypeJarHash: SecureHash?
)

data class FiatCurrencyIssueModel @JsonCreator constructor(
        val currency: String,
        val amount: Long,
        val recipient: String
)

data class CreateAndIssueHouseModel @JsonCreator constructor(
        val owner: String,
        val amount: Long,
        val currency: String,
        val noOfBedRooms: Int,
        val address: String
)

data class HouseSaleModel @JsonCreator constructor(
        val houseId: String,
        val buyer: String
)

data class UpdateHouseValuationModel @JsonCreator constructor(
        val houseId: String,
        val newAmount: Long,
        val newCurrency: String
)
