package com.template.states

import com.r3.corda.lib.tokens.contracts.types.TokenType
import com.template.types.UserContract
import net.corda.core.contracts.Amount
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.Party

@BelongsToContract(UserContract::class)
data class UserState(val name: String,
                     val wallet: MutableList<Amount<TokenType>>,
                     override val participants: List<Party>,
                     override val linearId: UniqueIdentifier = UniqueIdentifier()): LinearState
{
}