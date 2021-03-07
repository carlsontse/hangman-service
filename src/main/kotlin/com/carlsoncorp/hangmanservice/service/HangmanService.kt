package com.carlsoncorp.hangmanservice.service

import com.carlsoncorp.hangmanservice.model.Game
import com.carlsoncorp.hangmanservice.service.exception.DuplicateWrongGuessException
import com.carlsoncorp.hangmanservice.service.exception.GameAlreadyOverException
import com.carlsoncorp.hangmanservice.service.exception.GameNotFoundException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class HangmanService {

    // For now just implement in memory storage of games. This list resets when the service shuts down.
    val games: HashMap<String, Game> = HashMap()
    val LOGGER: Logger = LoggerFactory.getLogger(HangmanService::javaClass.javaClass)
    final val DEFAULT_MAX_NUM_GUESSES: Int = 10

    fun createNewGame(maxNumberOfGuessesInput: Int?, secretWordInput: String?): Game {

        var maxNumberOfGuesses = maxNumberOfGuessesInput
        var secretWord: String

        if (maxNumberOfGuesses == null) {
            maxNumberOfGuesses = DEFAULT_MAX_NUM_GUESSES
        }

        if (secretWordInput == null) {
            // randomly generate a word or hardcode for now TODO: instructions say read from file but can fallback as well to
                // a library
            secretWord = "carlson"
        } else {
            secretWord = secretWordInput
        }

        //TODO: properly fill out the sessionId
        val newGame = Game(maxNumberOfGuesses, secretWord, "tempSessionid")
        games[newGame.getId()] = newGame

        LOGGER.info("Starting a new game w/ id ({}), maxNumberOfGuesses ({}), secretWord ({})", newGame.getId(),
            maxNumberOfGuesses, secretWord)

        return newGame
    }

    /**
     * Lookup the game
     * @param id Unique identifier for the game
     * @throws GameNotFoundException
     */
    fun getGame(id: String): Game {
        // look up in db
        val game = games[id]

        if (game != null) {
            return game
        } else {
            LOGGER.error("Unable to find game with id {}", id)
            throw GameNotFoundException()
        }
    }

    /**
     * Get all the games.
     * TODO: can support filtering
     */
    fun getGames(isActive: Boolean): ArrayList<Game> =
        ArrayList(games.values)

    /**
     * Perform a guess
     * @param guessLetter letter being guessed. Assumption is that it's lower case.
     * @throws DuplicateWrongGuessException
     */
    fun guess(guessLetter: Char, gameId: String, sessionId: String): Game {

        val game = getGame(gameId)

        // check if the game is already finished
        if (game.isGameOver()) {
            LOGGER.info("Can't guess the letter because game w/ id ({}) is already over.", gameId)
            throw GameAlreadyOverException(game.getState())
        }

        if (!game.hasAlreadyGuessedLetter(guessLetter)) {
            game.updateGuessWordTracker(guessLetter)
        } else { // it's already been guessed wrong
            LOGGER.info("The letter ({}) was already guessed wrong for game w/ id ({}).", guessLetter, gameId)
            throw DuplicateWrongGuessException()
        }

        return game
    }

}