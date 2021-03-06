package com.carlsoncorp.hangmanservice.service

import com.carlsoncorp.hangmanservice.model.Game
import com.carlsoncorp.hangmanservice.service.exception.DuplicateWrongGuessException
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
            // randomly generate a word or hardcode for now
            secretWord = "carlson"
        } else {
            secretWord = secretWordInput
        }

        val newGame = Game(maxNumberOfGuesses, secretWord)
        games[newGame.id] = newGame
        return newGame
    }

    /**
     * @throws GameNotFoundException
     */
    fun getGame(id: String): Game {
        // look up in db
        val game = games.get(id)

        if (game != null) {
            return game
        } else {
            LOGGER.error("Unable to find game with id {}", id)
            throw GameNotFoundException()
        }
    }

    /**
     * Perform a guess
     * @throws DuplicateWrongGuessException
     */
    fun guess(guessLetter: Char, gameId: String, sessionId: String): Game {
        // handle same letter guesses, handle invalid inputs
        val game = getGame(gameId)

        // check if the game is already finished
        if (game.isGameOver()) {
            throw GameAlreadyOverException(game.state) // todo: indicate if already won or lost number of guesses
        }

        val wrongGuessesList = game.wrongGuessesList

        if (!wrongGuessesList.contains(guessLetter)) { // This is technically a constant lookup since we're fixed set of letters

            val secretWord = game.secretWord
            var guessingWordTracker = game.guessingWordTracker

            for (charIndex in secretWord.indices) {
                if (secretWord[charIndex].toLowerCase() === guessLetter) { // ignore the case on the secret word
                    // if it matches then flip it in the guessWordTracker
                    guessingWordTracker[charIndex] = secretWord[charIndex] // Keep the original case
                }
            }

            //if guess is wrong

            // O(N) with the dynamic array resizing
            wrongGuessesList.add(guessLetter)
        } else { // it's already been guessed wrong
            throw DuplicateWrongGuessException()
        }


        return game
    }

}