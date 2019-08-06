package com.template.tokenSdk

import com.r3.corda.lib.tokens.contracts.states.EvolvableTokenType
import com.r3.corda.lib.tokens.contracts.types.TokenPointer
import com.r3.corda.lib.tokens.contracts.types.TokenType
import net.corda.core.contracts.Amount
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearPointer
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.Party

@BelongsToContract(HouseContract::class)
data class HouseState(
        val valuation: Amount<TokenType>,
        override val maintainers: List<Party>,
        override val fractionDigits: Int = 0,
        override val linearId: UniqueIdentifier = UniqueIdentifier()
) :  EvolvableTokenType(){

    companion object {
        /* This method returns a TokenPointer by using the linear Id of the evolvable state */
        fun toPointer(houseState: HouseState): TokenPointer<HouseState> {
            val linearPointer = LinearPointer(houseState.linearId, HouseState::class.java)
            return TokenPointer(linearPointer, houseState.fractionDigits)
        }

    }
}

