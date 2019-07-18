package com.template.flows.userwithschedulestate

import net.corda.core.contracts.*
import net.corda.core.flows.FlowLogicRefFactory
import net.corda.core.identity.Party
import java.time.Instant

@BelongsToContract(RegisterContract::class)
class RegisterState(val initiator: Party,
                    val verify: Boolean,
                    val requestTime: Instant,
                    val delay: Long,
                    override val linearId: UniqueIdentifier = UniqueIdentifier()): SchedulableState,LinearState
{
    override val participants get() = listOf(initiator)
    override fun nextScheduledActivity(thisStateRef: StateRef, flowLogicRefFactory: FlowLogicRefFactory): ScheduledActivity?{
        val responseTime = requestTime.plusSeconds(10)
        return ScheduledActivity(flowLogicRefFactory.create(VerifyRegisterFlow::class.java,thisStateRef),responseTime)
    }

}