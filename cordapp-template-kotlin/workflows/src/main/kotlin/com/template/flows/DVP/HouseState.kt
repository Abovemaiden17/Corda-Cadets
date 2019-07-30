package com.template.flows.DVP

import com.r3.corda.lib.tokens.contracts.states.EvolvableTokenType
import com.r3.corda.lib.tokens.contracts.types.TokenType
import net.corda.core.contracts.Amount
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable

@BelongsToContract(HouseContract::class)
@CordaSerializable
data class HouseState(val mayAri: Party,
                      val address: String,
                      val valuation: Amount<TokenType>,
                      override val maintainers: List<Party>,
                      override val fractionDigits: Int = 0,
                      override val linearId: UniqueIdentifier) : EvolvableTokenType()






