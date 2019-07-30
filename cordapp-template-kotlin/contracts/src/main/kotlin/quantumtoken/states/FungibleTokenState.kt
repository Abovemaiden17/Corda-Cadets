package quantumtoken.states

import com.r3.corda.lib.tokens.contracts.FungibleTokenContract
import com.r3.corda.lib.tokens.contracts.states.FungibleToken
import com.r3.corda.lib.tokens.contracts.types.IssuedTokenType
import com.r3.corda.lib.tokens.contracts.utilities.getAttachmentIdForGenericParam
import net.corda.core.contracts.Amount
import net.corda.core.contracts.BelongsToContract
import net.corda.core.crypto.SecureHash
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party


@BelongsToContract(FungibleTokenContract::class)
class FungibleTokenState (override val amount: Amount<IssuedTokenType>,
                          override val holder: Party,
                          override val tokenTypeJarHash: SecureHash? = amount.token.tokenType.getAttachmentIdForGenericParam()
                        ): FungibleToken(amount, holder)