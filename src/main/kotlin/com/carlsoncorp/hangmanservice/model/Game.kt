package com.carlsoncorp.hangmanservice.model

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

enum class GameState {
    NEW_GAME, GAME_OVER_WIN, GAME_OVER_LOSS
}

class Game(private val maxNumberOfGuesses: Int,
           private val secretWord: String,
           // 'Who' started the game
           private val sessionId: String) {

    final val LOGGER: Logger = LoggerFactory.getLogger(Game::javaClass.javaClass)
    final val DEFAULT_MASK_CHAR = '*'

    private val id: String = UUID.randomUUID().toString()
    private var state: GameState = GameState.NEW_GAME

    // Game auditing fields
    private val startTimeMs: Long = System.currentTimeMillis()
    private var endTimeMs: Long? = null
    private var lastGuessTimeMs: Long? = null // Can be used to clean up games that havent had guesses in a while.

    /** We could fix this to an array of size 26 but to keep the game extensible such as taking numbers, or
        punctuation characters in the future it's a worthy tradeoff.
    **/
    private var wrongGuessesList: ArrayList<Guess> = ArrayList<Guess>()

    // Track the word being guessed
    private var guessingWordTracker: CharArray

    // Forward thinking.
    // TBH i have no idea how hangman in other languages would even work...
    private var locale: Locale = Locale.ENGLISH
    // Would be fun to let clients name the game 'room' and to support the list of games going on.
    private var name: String = "default"

    init {
        // initialize the guessing word tracker to same length as the secret word and initalize to the default char
        guessingWordTracker = CharArray(secretWord.length) {DEFAULT_MASK_CHAR}
    }

    fun getId() =
        id

    fun getState() =
        state

    fun getGuessingWordTracker() =
        guessingWordTracker

    fun getWrongGuesses() =
        wrongGuessesList

    fun getNumberOfRemainingGuesses() =
        maxNumberOfGuesses - wrongGuessesList.size

    /**
     * Update the guess word tracker if the letter is found, if not then add to the wrong guesses list.
     * @param guessLetter letter being guessed
     *
     */
    fun updateGuessWordTracker(guessLetter: Char) {

        lastGuessTimeMs = System.currentTimeMillis()

        // Track whether we found the guess letter so we know it's not a wrong guess
        var foundGuessLetter = false

        // TODO: could prob do something fancy here to keep track of the correctly guessed letters to save the O(N)
        // in case someone keeps guessing the same letter for no reason, it's an edge case.
        for (charIndex in secretWord.indices) {
            if (secretWord[charIndex].toLowerCase() === guessLetter) { // ignore the case on the secret word
                // if it matches then flip it in the guessWordTracker.
                guessingWordTracker[charIndex] = secretWord[charIndex] // Keep the original case
                foundGuessLetter = true
            }
        }

        // if guess is wrong
        if (!foundGuessLetter) {
            LOGGER.info("Guess letter ({}) is wrong w/ game id ({})", guessLetter, id)
            addWrongGuess(guessLetter)
        } else {
            // guess was right
            LOGGER.info("Guess letter ({}) is right w/ game id ({}). New tracker string = {}", guessLetter, id, guessingWordTracker)
            // check if the game has been won
            if (!anymoreLettersToFind()) {
                LOGGER.info("Word ({}) has been completely revealed and game w/ id ({}) is over!",
                    secretWord, id)
                endGame(GameState.GAME_OVER_WIN)
            }
        }
    }

    /**
     * Game is won when there are no more of the DEFAULT_TRACKER_MASK in the tracker string
     */
    private fun anymoreLettersToFind(): Boolean =
        guessingWordTracker.contains(DEFAULT_MASK_CHAR)

    fun hasAlreadyGuessedLetter(guessLetter: Char) =
        wrongGuessesList.stream().anyMatch{ t -> t.guessLetter === guessLetter}

    private fun addWrongGuess(guessLetter: Char) {
        //todo: get the session ID
        wrongGuessesList.add(Guess(
            guessLetter,
            System.currentTimeMillis(),
            "tempSessionId"
        ))

        if (wrongGuessesList.size === maxNumberOfGuesses) {
            LOGGER.info("Number of wrong guesses has reached the max of ({}) w/ game id ({}). Game over!",
                maxNumberOfGuesses, id)
            endGame(GameState.GAME_OVER_LOSS)
        }
    }

    private fun endGame(newState: GameState) {
        state = newState
        endTimeMs = System.currentTimeMillis()
    }

    fun isGameOver(): Boolean =
        state === GameState.GAME_OVER_LOSS || state === GameState.GAME_OVER_WIN

}