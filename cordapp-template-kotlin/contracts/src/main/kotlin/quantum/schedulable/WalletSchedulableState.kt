package quantum.schedulable

import net.corda.core.contracts.*
import net.corda.core.flows.FlowLogicRefFactory
import net.corda.core.identity.Party
import java.time.Instant

@BelongsToContract(WalletSchedulableContract::class)
class WalletSchedulableState(val registrant: Party,
                             val approved: Boolean,
                             val requestTime: Instant,
                             val delay: Long,
                             override val linearId: UniqueIdentifier) : SchedulableState, LinearState
{
    override val participants: List<Party> get() = listOf(registrant)
    override fun nextScheduledActivity(thisStateRef: StateRef, flowLogicRefFactory: FlowLogicRefFactory): ScheduledActivity?
    {
        val responseTime: Instant = requestTime.plusSeconds(delay)
        return ScheduledActivity(flowLogicRefFactory.create(
                WalletSchedulableVerifyFlow::class.java, thisStateRef),
                responseTime
        )
    }
}

//data class LinearIdWalletState(val registrant: Party,
//                               val approved: Boolean,
//                               override val linearId: UniqueIdentifier,
//                               override val participants: List<Party>): LinearState
