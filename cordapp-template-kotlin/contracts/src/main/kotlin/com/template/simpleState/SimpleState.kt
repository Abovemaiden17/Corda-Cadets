package com.template.simpleState

import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.Party

@BelongsToContract(SimpleContract::class)
data class SimpleState(
        val name: String,
        val age: Int,
        val sender: Party,
        val receiver: Party,
        override val linearId: UniqueIdentifier = UniqueIdentifier(),
        override val participants: List<Party> = listOf(sender,receiver)
):LinearState