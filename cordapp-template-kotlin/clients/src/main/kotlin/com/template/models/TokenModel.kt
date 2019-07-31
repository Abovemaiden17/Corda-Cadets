package com.template.models

import com.r3.corda.lib.tokens.contracts.types.IssuedTokenType
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty

data class TokenModel(val token: IssuedTokenType,
                      val holder: AbstractParty,
                      val linearId: UniqueIdentifier,
                      val tokentype :String)