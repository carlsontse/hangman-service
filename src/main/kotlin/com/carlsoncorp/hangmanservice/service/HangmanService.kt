package com.carlsoncorp.hangmanservice.service

import com.carlsoncorp.hangmanservice.model.Game
import com.carlsoncorp.hangmanservice.service.exception.DuplicateWrongGuessException
import com.carlsoncorp.hangmanservice.service.exception.GameAlreadyOverException
import com.carlsoncorp.hangmanservice.service.exception.GameNotFoundException
import com.carlsoncorp.hangmanservice.service.exception.NotPlayerTurnException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class HangmanService {

    val LOGGER: Logger = LoggerFactory.getLogger(HangmanService::class.java)
    final val DEFAULT_MAX_NUM_GUESSES: Int = 10

    // For now just implement in memory storage of games. This list resets when the service shuts down.
    val games: HashMap<String, Game> = HashMap()

    /**
     * Create a new game
     * @param sessionId unique player id
     * @param maxNumberOfGuessesInput set the max number of guesses
     * @param secretWordInput set the secret word
     * @return new instance of Hangman game
     */
    fun createNewGame(sessionId: String, maxNumberOfGuessesInput: Int?, secretWordInput: String?): Game {

        var maxNumberOfGuesses = maxNumberOfGuessesInput
        var secretWord: String

        if (maxNumberOfGuesses == null) {
            maxNumberOfGuesses = DEFAULT_MAX_NUM_GUESSES
        }

        if (secretWordInput == null) {
            // TODO: instructions say read from file on server, for now intentionally hardcoding
            secretWord = "hangman"
        } else {
            secretWord = secretWordInput
        }

        val newGame = Game(maxNumberOfGuesses, secretWord, sessionId)
        games[newGame.getId()] = newGame

        LOGGER.info("Starting a new game w/ id ({}), maxNumberOfGuesses ({}), secretWord ({})", newGame.getId(),
            maxNumberOfGuesses, secretWord)

        // Remove the player from any other previous game they were associated with
        games.values.map {
            if (it.getId() != newGame.getId()) { // don't remove the player from the new game just created
                it.removePlayerIfFound(sessionId)
            }
        }

        return newGame
    }

    /**
     * Lookup the game and also add the player if it's a newly detected valid sessionId.
     * @param id Unique identifier for the game
     * @param sessionId Unique session identifier. Could indicate a new player. If null just return the game.
     * @throws GameNotFoundException
     * @return Game that contains the specified id
     */
    fun getGame(id: String, sessionId: String?): Game {
        // look up in db
        val game = games[id]

        if (game != null) {
            if (sessionId != null) {
                game.addPlayerIfNew(sessionId)
            }
            return game
        } else {
            LOGGER.info("Unable to find game with id {}", id)
            throw GameNotFoundException()
        }
    }

    /**
     * Get the games
     * @param isActive filter by active games only
     * @return stored games
     */
    fun getGames(isActive: Boolean): ArrayList<Game> =
        ArrayList(games.values.filter {
            if (isActive) {
                // return only active games
                !it.isGameOver()
            } else {
                // return everything
                true
            }
        })

    /**
     * Perform a guess
     * @param guessLetter letter being guessed. Assumption is that it's lower case.
     * @param gameId unique identifier for the game
     * @param sessionId unique identifier for the player
     * @throws DuplicateWrongGuessException
     * @return Game with the new guess applied (if valid)
     */
    //TODO: think of some multi threading cases regarding the players and guessing!!
    fun guess(guessLetter: Char, gameId: String, sessionId: String): Game {

        // Make sure to not add the player to the game in case of a random player not part of the game guessing
        val game = getGame(gameId, null)

        // check if the game is already finished
        if (game.isGameOver()) {
            LOGGER.info("Can't guess the letter because game w/ id ({}) is already over.", gameId)
            throw GameAlreadyOverException(game.getState())
        }

        if (!game.isPlayerTurn(sessionId)) {
            //TODO: not doing it for now but we may want to differentiate a player that's not even in the game as well as
            // a different error msg. For now though this covers that case as well.
            LOGGER.info("Player ({}) tried to take a turn that is not their turn. Expected next player ({}) for game" +
                    " w/ id ({}).", sessionId, game.getNextPlayer(), gameId)
            throw NotPlayerTurnException()
        }

        if (!game.hasAlreadyGuessedLetter(guessLetter)) {
            game.updateGuessWordTracker(guessLetter, sessionId)
        } else { // it's already been guessed wrong
            LOGGER.info("The letter ({}) was already guessed wrong for game w/ id ({}).", guessLetter, gameId)
            throw DuplicateWrongGuessException()
        }

        return game
    }

}