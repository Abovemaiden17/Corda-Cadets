package com.template.houseDVP

import com.r3.corda.lib.tokens.contracts.states.EvolvableTokenType
import com.r3.corda.lib.tokens.contracts.types.TokenPointer
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.Party
import com.template.HouseContract
import net.corda.core.contracts.Amount
import java.util.*
import net.corda.core.contracts.LinearPointer


@BelongsToContract(HouseContract::class)
data class HouseDVPState(
        val owner: Party,
        override val fractionDigits: Int = 0,
        override val linearId: UniqueIdentifier,
        override val maintainers: List<Party>,

        //Properties of House State. Some of these values may evolve over time.
        val valuation: Amount<Currency>, val noOfBedRooms: Int, val address: String) : EvolvableTokenType() {


      fun pointer(): TokenPointer<HouseDVPState> {
        val linearPointer = LinearPointer(linearId, HouseDVPState::class.java)
        return TokenPointer(linearPointer, fractionDigits)
    }
}