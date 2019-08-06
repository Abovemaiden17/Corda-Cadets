//
//import com.r3.corda.lib.tokens.contracts.types.TokenPointer
//import net.corda.core.contracts.LinearPointer
//import net.corda.core.identity.Party
//import net.corda.core.contracts.Amount
//import net.corda.core.contracts.UniqueIdentifier
//import com.r3.corda.lib.tokens.contracts.states.EvolvableTokenType
//import com.template.TokenSDKsample.HouseContract
//import net.corda.core.contracts.BelongsToContract
//
//import com.r3.corda.lib.tokens.contracts.states.EvolvableTokenType
//import com.r3.corda.lib.tokens.contracts.types.TokenPointer
//import com.template.TokenSDKsample.HouseContract
//import net.corda.core.contracts.Amount
//import net.corda.core.contracts.BelongsToContract
//import net.corda.core.contracts.LinearPointer
//import net.corda.core.contracts.UniqueIdentifier
//import net.corda.core.identity.Party
//import org.jetbrains.annotations.NotNull
//import java.util.*
//
//@BelongsToContract(HouseContract::class)
//class HouseState(
//                 override val linearId: UniqueIdentifier,
//                 override private val maintainers: List<Party>, //Properties of House State. Some of these values may evolve over time.
//                 val valuation: Amount<Currency>,
//                 val noOfBedRooms: Int,
//                 val constructionArea: String,
//                 val additionInfo: String,
//                 val address: String) : EvolvableTokenType() {
//
//    val issuer: Party
//    override val fractionDigits = 0
//
//    init {
//        issuer = maintainers[0]
//    }
//
//    @NotNull
//    override fun getMaintainers(): List<Party> {
//        return ImmutableList.copyOf(maintainers)
//    }
//
//    /* This method returns a TokenPointer by using the linear Id of the evolvable state */
//    fun toPointer(): TokenPointer<HouseState> {
//        val linearPointer = LinearPointer(linearId, HouseState::class.java)
//        return TokenPointer(linearPointer, fractionDigits)
//    }
//}