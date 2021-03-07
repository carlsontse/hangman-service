package com.carlsoncorp.hangmanservice.model

// A guess data object to track the guessed letters and some other analytic data
data class Guess (
    val guessLetter: Char,
    val timeOfGuess: Long,
    // For now used to track 'who' made the guess
    val sessionId: String
)