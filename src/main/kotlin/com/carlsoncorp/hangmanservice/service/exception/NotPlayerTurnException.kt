package com.carlsoncorp.hangmanservice.service.exception

import java.lang.RuntimeException

/**
 * Indicates a player took a turn that was not their turn
 */
class NotPlayerTurnException: RuntimeException() {

    init {
    }
}