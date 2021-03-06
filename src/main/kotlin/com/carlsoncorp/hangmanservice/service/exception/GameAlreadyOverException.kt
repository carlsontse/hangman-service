package com.carlsoncorp.hangmanservice.service.exception

import com.carlsoncorp.hangmanservice.model.GameState

/**
 * Indicates a game is already completed.
 */
class GameAlreadyOverException(val state: GameState): RuntimeException() {
    init{

    }
}