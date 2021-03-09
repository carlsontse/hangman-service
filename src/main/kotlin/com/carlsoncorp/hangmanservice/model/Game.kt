package com.carlsoncorp.hangmanservice.model

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

enum class GameState {
    NEW_GAME, GAME_OVER_WIN, GAME_OVER_LOSS
}

class Game(private val maxNumberOfGuesses: Int,
           private val secretWord: String,
           // 'Who' started the game.
           private val creatorSessionId: String) {

    final val LOGGER: Logger = LoggerFactory.getLogger(Game::class.java)
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

    /** Track the active session players as a stack. The notes say only 2 players but let's make it extensible.
     **/
    // Whenever someone 'loads' the game with a new sessionId, we assume it's a 'player entered' state.
    // For now, storing as a String but we could expand this to a 'Player' class as we evolve the game.
    private var players: ArrayList<String> = ArrayList<String>()
    // Track the active player as the index of the players list
    private var activePlayerIndex: Int
    // Forward thinking: A player could 'abandon' a game by just leaving it alone or closing browser, we should have a
    // time limit to move and remove them as a player when the time has elapsed.

    // Forward thinking.
    // TBH i have no idea how hangman in other languages would even work...
    private var locale: Locale = Locale.ENGLISH
    // Would be fun to let clients name the game 'room' and to support the list of games going on.
    private var name: String = "default"

    init {
        // initialize the guessing word tracker to same length as the secret word and initalize to the default char
        guessingWordTracker = CharArray(secretWord.length) {DEFAULT_MASK_CHAR}

        // initialize the active player to the person that created the game
        players.add(creatorSessionId)
        // when the game starts we expect the creator to make the first move
        activePlayerIndex = 0
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

    fun getPlayers() =
        players

    /**
     * Update the guess word tracker if the letter is found, if not then add to the wrong guesses list.
     * @param guessLetter letter being guessed
     * @param sessionId Unique player identifier
     *
     */
    fun updateGuessWordTracker(guessLetter: Char, sessionId: String) {

        lastGuessTimeMs = System.currentTimeMillis()

        // Track whether we found the guess letter so we know it's not a wrong guess
        var foundGuessLetter = false

        // TODO: could prob do something fancy here to keep track of the correctly guessed letters to save the O(N)
        // in case someone keeps guessing the same letter for no reason, it's an edge case.
        for (charIndex in secretWord.indices) {
            if (secretWord[charIndex].toLowerCase() == guessLetter) { // ignore the case on the secret word
                // if it matches then flip it in the guessWordTracker.
                guessingWordTracker[charIndex] = secretWord[charIndex] // Keep the original case
                foundGuessLetter = true
            }
        }

        // if guess is wrong
        if (!foundGuessLetter) {
            LOGGER.info("Guess letter ({}) is wrong w/ game id ({})", guessLetter, id)
            addWrongGuess(guessLetter, sessionId)
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

        // advance the expected player to the next person
        //todo: think about the race/thread conditions here as well, the players could change as we remove them or they join
        activePlayerIndex = (activePlayerIndex + 1) % players.size
    }

    /**
     * Game is won when there are no more of the DEFAULT_TRACKER_MASK in the tracker string
     */
    private fun anymoreLettersToFind(): Boolean =
        guessingWordTracker.contains(DEFAULT_MASK_CHAR)

    fun hasAlreadyGuessedLetter(guessLetter: Char) =
        wrongGuessesList.stream().anyMatch{ t -> t.guessLetter == guessLetter}

    private fun addWrongGuess(guessLetter: Char, sessionId: String) {
        wrongGuessesList.add(Guess(
            guessLetter,
            System.currentTimeMillis(),
            sessionId
        ))

        if (wrongGuessesList.size == maxNumberOfGuesses) {
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
        state == GameState.GAME_OVER_LOSS || state == GameState.GAME_OVER_WIN

    /**
     * Add new player if they are not currently in the game and only if the game is not over.
     * @param sessionId player identifier
     */
    fun addPlayerIfNew(sessionId: String) {
        // no need to throw exception if game is over, the state captures the game state.
        if (!players.contains(sessionId) && !isGameOver()) {
            LOGGER.info("New player entered the game w/ id ({}) for game /w id ({}).", sessionId, id)
            players.add(sessionId)
        }

        if (isGameOver()) {
            LOGGER.info("Tried to add a new player w/ id ({}) when the game w/ id ({}) is already over.", sessionId, id)
        }
    }

    /**
     * Determine if it is the specified player's turn.
     * @param sessionId player identifier
     */
    fun isPlayerTurn(sessionId: String): Boolean =
        players[activePlayerIndex] == sessionId

    /**
     * Get the next player's turn's identifier
     * @return String next player turn's identifier
     */
    fun getNextPlayer(): String =
        players[activePlayerIndex]

    /**
     * Remove the player if they are found in the game.
     * @param sessionId player identifier
     */
    fun removePlayerIfFound(sessionId: String) {
        val playerIndex = players.indexOf(sessionId)

        if (playerIndex >= 0) { // player was found
            // all elements get shifted down so no need to update the activePlayerIndex if the active player is the person
            // being removed. Otherwise if it's not then we do need to update the activePlayerIndex
            if (playerIndex !== activePlayerIndex && activePlayerIndex > 0) {
                activePlayerIndex -= 1
            }
            players.remove(sessionId)

        }
    }
}