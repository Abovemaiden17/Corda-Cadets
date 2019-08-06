package com.template.tokenSdk

import com.r3.corda.lib.tokens.contracts.types.TokenType
import net.corda.core.contracts.Amount
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.Party

@BelongsToContract(UserContract::class)
data class UserState(val name: String,
//                     val wallet: MutableList<Amount<TokenType>>,
                     var wallet: MutableList<Amount<TokenType>>,
                     override val participants: List<Party>,
                     override var linearId: UniqueIdentifier = UniqueIdentifier()): LinearState