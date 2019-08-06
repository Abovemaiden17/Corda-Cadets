package com.template.increment

import net.corda.core.contracts.*
import net.corda.core.flows.FlowLogicRefFactory

import net.corda.core.identity.Party
import java.time.Instant

@BelongsToContract(IncrementContract::class)
data class IncrementState(
        val name: String,
        val balance: Long,
        val owner: Party,
        val scheduledTime: Instant,
        override val participants: List<Party> = listOf(owner),
        override val linearId: UniqueIdentifier = UniqueIdentifier()
):SchedulableState,LinearState {
    override fun nextScheduledActivity(thisStateRef: StateRef, flowLogicRefFactory: FlowLogicRefFactory): ScheduledActivity? {
        return ScheduledActivity(flowLogicRefFactory.create(IncrementValueFlow::class.java, thisStateRef), scheduledTime)
    }
}