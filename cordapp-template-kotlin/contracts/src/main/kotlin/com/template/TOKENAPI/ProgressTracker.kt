package com.template.TOKENAPI

import net.corda.core.flows.FinalityFlow
import net.corda.core.utilities.ProgressTracker

object GENERATING_TRANSACTION : ProgressTracker.Step("Generating transaction.")
object SIGNING_TRANSACTION : ProgressTracker.Step("Signing transaction with our private key.")
object VERIFYING_TRANSACTION : ProgressTracker.Step("Verifying contract constraints.")
object NOTARIZE_TRANSACTION : ProgressTracker.Step("Notarizing Transaction")
object FINALISING_TRANSACTION : ProgressTracker.Step("Recording transaction.") {
    override fun childProgressTracker() = FinalityFlow.tracker()
}