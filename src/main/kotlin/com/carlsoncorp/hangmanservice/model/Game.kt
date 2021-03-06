package com.carlsoncorp.hangmanservice.model

import java.util.*

enum class GameState {
    NEW_GAME, GAME_OVER_WIN, GAME_OVER_LOSS
}

class Game() {

    val id: String = UUID.randomUUID().toString()
    val state: GameState = GameState.NEW_GAME

    // These fields would be used to clean up abandoned games
    val startTimeMs: Long = System.currentTimeMillis()
    val lastGuessTimeMs: Long? = null

    // Future proof. TBH i have no idea how hangman in other languages would even work...
    var locale: Locale = Locale.ENGLISH

    /** We could fix this to an array of size 26 but to keep the game extensible such as taking numbers, or
        punctuation characters in the future it's a worthy tradeoff.
    **/
    var wrongGuesses: ArrayList<Char> = ArrayList<Char>()

    /**
     *
     */
    var wordState: ArrayList<Char> = ArrayList<Char>()

    /**
     * Store the hashmap to
     */
    var letterToPositionMap: HashMap<Char, List<Int>> = HashMap<Char, List<Int>>()

    lateinit var word: String

    init {
        //TODO: change this
        word = "carlson"
    }
}