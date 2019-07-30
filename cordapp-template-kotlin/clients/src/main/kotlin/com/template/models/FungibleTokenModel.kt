package com.template.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.r3.corda.lib.tokens.contracts.types.IssuedTokenType
import net.corda.core.contracts.Amount
import net.corda.core.crypto.SecureHash
import net.corda.core.identity.AbstractParty

data class FungibleTokenModel (
        val amount: Amount<IssuedTokenType>,
        val holder: AbstractParty,
        val tokenTypeJarHash: SecureHash?
)