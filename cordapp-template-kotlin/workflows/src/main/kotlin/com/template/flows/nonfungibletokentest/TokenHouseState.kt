package com.template.flows.nonfungibletokentest

import com.r3.corda.lib.tokens.contracts.states.EvolvableTokenType
import com.r3.corda.lib.tokens.contracts.types.TokenType
import net.corda.core.contracts.Amount
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable

import java.util.Currency

@BelongsToContract(TokenHouseContract::class)
data class TokenHouseState(val owner: Party,
                           val address: String,
                           val valuation: Amount<TokenType>,
                           override val fractionDigits: Int = 0,
                           override val maintainers: List<Party>,
                           override val linearId: UniqueIdentifier = UniqueIdentifier()) : EvolvableTokenType()
@CordaSerializable
data class PriceNotification(val amount: Amount<TokenType>)
