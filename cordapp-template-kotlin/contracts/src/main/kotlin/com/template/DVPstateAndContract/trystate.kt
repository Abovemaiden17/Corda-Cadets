package com.template.DVPstateAndContract

import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.serialization.CordaSerializable

@BelongsToContract(trycontract::class)

@CordaSerializable
data class trystate(val rates: Amount,
                    val base: String,
                    val date: String,
                    override val linearId: UniqueIdentifier,
                    override val participants: List<AbstractParty>) : LinearState {


}

@CordaSerializable
data class Amount(val basecurrency: String, val baseamount: Int, val convertcurrency: String, val convertamount: Int)