package com.template.Token

import com.r3.corda.lib.tokens.contracts.states.EvolvableTokenType
import com.r3.corda.lib.tokens.contracts.types.TokenType
import net.corda.core.contracts.Amount
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.Party

@BelongsToContract(TokenContract::class)
data class TokenState(
//        val amountToBorrow: Long,
        val address: String,
        val valuation: Amount<TokenType>,
        val amountIssued: Long,
        val amountPaid: Long,
        val borrower: Party,
        val lender: Party,
        val iss: Party,
        val walletBalance: Long,
        val approve: Boolean = false,
        val settled: Boolean = false,
        override val linearId: UniqueIdentifier = UniqueIdentifier(),

        override val fractionDigits: Int = 0,
        override val maintainers: List<Party> = listOf(lender,borrower)
      ): LinearState , EvolvableTokenType()
