package quantum.states

import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.Party
import quantum.contracts.TimeWindowContract

@BelongsToContract(TimeWindowContract::class)
data class TimeWindowState(val status: Boolean,
                           val party: Party,
                           val counter: Party,
                           override val participants: List<Party> = listOf(party, counter),
                           override val linearId: UniqueIdentifier = UniqueIdentifier()): LinearState