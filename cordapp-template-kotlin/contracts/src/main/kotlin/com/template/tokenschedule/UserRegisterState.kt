package com.template.tokenschedule

import net.corda.core.contracts.*
import net.corda.core.flows.FlowLogicRefFactory
import net.corda.core.identity.Party
import java.time.Instant

@BelongsToContract(UserRegisterContract::class)
open class UserRegisterState(
        val initiator: Party,
        val approved: Boolean,
        val activityTimeNext: Instant,
        override val linearId: UniqueIdentifier
) : SchedulableState, LinearState {


    override val participants get() = listOf(initiator)

    // Defines the scheduled activity to be conducted by the SchedulableState.
    override fun nextScheduledActivity(thisStateRef: StateRef, flowLogicRefFactory: FlowLogicRefFactory): ScheduledActivity? {
        // A heartbeat will be emitted every second.
        // We get the time when the scheduled activity will occur in the constructor rather than in this method. This is
        // because calling Instant.now() in nextScheduledActivity returns the time at which the function is called, rather
        // than the time at which the state was created.
        return ScheduledActivity(flowLogicRefFactory.create(UserRegisterVerifyFlow::class.java, thisStateRef), activityTimeNext)
    }

}