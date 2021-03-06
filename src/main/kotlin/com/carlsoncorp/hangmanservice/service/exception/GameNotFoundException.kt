package com.carlsoncorp.hangmanservice.service.exception

import java.lang.RuntimeException

/**
 * Indicates a game was not found.
 */
class GameNotFoundException: RuntimeException() {

    init {
    }
}