package com.template.TOKENAPI

import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable

@CordaSerializable
@BelongsToContract(tokenAPIcontract::class)
data class tokenAPIstate(
//        val amountToBorrow: Long,
        val tokenName: String,
        val tokenValue: Int,
        val initiator: Party,
        override val linearId: UniqueIdentifier = UniqueIdentifier(),
        override val participants: List<Party> = listOf(initiator)
) : LinearState
