package quantum.schedulable

import net.corda.core.contracts.*
import net.corda.core.flows.FlowLogicRefFactory
import net.corda.core.identity.Party
import java.time.Instant

@BelongsToContract(WalletSchedulableContract::class)
class WalletSchedulableState(val initiator: Party,
                             val amount: Long,
                             private val nextActivityTime: Instant = Instant.now().plusSeconds(10)) : SchedulableState
{
    override val participants: List<Party> get() = listOf(initiator)
    override fun nextScheduledActivity(thisStateRef: StateRef, flowLogicRefFactory: FlowLogicRefFactory): ScheduledActivity?
    {
        return ScheduledActivity(flowLogicRefFactory.create(
                WalletSchedulableSelfIssueFlow::class.java, thisStateRef),
                nextActivityTime
        )
    }
}
