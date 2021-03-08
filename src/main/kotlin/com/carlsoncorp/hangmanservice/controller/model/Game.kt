package com.carlsoncorp.hangmanservice.controller.model

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * External model representation of the Game state.
 * Reminder: Don't return the secret word!
 */
@ApiModel(description = "Class representing a state of a Hangman game")
data class Game(
    @ApiModelProperty(notes = "UUID of the game. No two games can have the same id.", example = "51d54854-528a-4411-943e-c1de39f5704b", required = true, position = 0)
    val id: String,

    @ApiModelProperty(notes = "Current state of the secret word guess. '*' is used to indicate unrevealed characters.", example = "*a****a*", required = true, position = 1)
    val guessingWordTracker: CharArray,

    @ApiModelProperty(notes = "List of the wrongly guessed letters", example = "bc", required = true, position = 2)
    val wrongGuesses: CharArray,

    @ApiModelProperty(notes = "Indicates the state of the game.", example = "NEW_GAME", required = true, position = 3)
    val gameState: String,

    @ApiModelProperty(notes = "Indicates the number of guesses left until the game is over.", example = "8", required = true, position = 4)
    val numGuessesLeft: Int,

    @ApiModelProperty(notes = "Unique identifier to indicate which player's turn. For now, it's a session id",
        example = "b2eb5388-19e4-45f9-b02e-ebaae4b22ddd", required = true, position = 5)
    val playerTurn: String
)
