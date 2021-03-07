package com.carlsoncorp.hangmanservice.controller.model

import java.util.*

/**
 * Contextual information used for logging, analytics
 */
class Context (
    val sessionId: String,
    val locale: Locale,
    val device: Device,
    val browserInformation: BrowserInformation

)

class Device (
)

class BrowserInformation (

)