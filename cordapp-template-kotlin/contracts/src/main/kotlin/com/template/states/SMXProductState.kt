package com.template.states

import com.template.types.SMXProductContract
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import java.time.Instant

@BelongsToContract(SMXProductContract::class)
data class SMXProductState(val id: Int,
                           val productName : String,
                           val smx_serial_num: Int,
                           val owner: Party,
                           val date: Instant,
                           override val participants: List<Party>,
                           override val linearId: UniqueIdentifier = UniqueIdentifier()): LinearState
