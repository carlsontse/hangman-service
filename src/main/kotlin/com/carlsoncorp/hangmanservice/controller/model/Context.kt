package com.carlsoncorp.hangmanservice.controller.model

/**
 * Contextual information used for logging, analytics
 */
class Context (
    val sessionId: String,
    val device: Device,
    val browserInformation: BrowserInformation

)

class Device (
)

class BrowserInformation (

)