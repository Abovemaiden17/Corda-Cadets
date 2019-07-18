package quantum.schedulable

import net.corda.core.contracts.Command
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.*
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import java.time.Instant
import java.util.*

@InitiatingFlow
@StartableByRPC
class WalletSchedulableRegisterFlow (private val delay: Long): WalletSchedulableFunctions()
{
    override fun call(): SignedTransaction
    {
        val registration = register()
        val signedTx = verifyAndSign(registration)
        return recordTransactionWithOtherParty(signedTx, listOf())
    }

    private fun outState() : WalletSchedulableState
    {
        return WalletSchedulableState(
                registrant = ourIdentity,
                approved = false,
                requestTime = Instant.now(),
                delay = delay,
                linearId = UniqueIdentifier()
        )
    }

    private fun register() : TransactionBuilder =
            TransactionBuilder(notary = serviceHub.networkMapCache.notaryIdentities.first()).apply{
                addOutputState(outState(), WalletSchedulableContract.WALLETSCHEDULE_ID)
                addCommand(Command(WalletSchedulableContract.Commands.Register(), ourIdentity.owningKey))
            }
}