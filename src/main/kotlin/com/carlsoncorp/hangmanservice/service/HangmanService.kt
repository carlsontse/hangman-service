package com.carlsoncorp.hangmanservice.service

import com.carlsoncorp.hangmanservice.model.Game
import com.carlsoncorp.hangmanservice.service.exception.GameNotFoundException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class HangmanService {

    // For now just implement in memory storage of games. This list resets when the service shuts down.
    val games: HashMap<String, Game> = HashMap()
    val LOGGER: Logger = LoggerFactory.getLogger(HangmanService::javaClass.javaClass)

    fun createNewGame(): Game {
        val newGame = Game()
        games.put(newGame.id, newGame)
        return newGame
    }

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
     *
     */
    fun guess(letter: Char, gameId: String, sessoinId: String): Game {
        // handle same letter guesses, handle invalid inputs
        val game = getGame(gameId)

        if (!game.wrongGuesses.contains(letter)) { // This is technically a constant lookup since we're fixed set of letters



            //if guess is wrong

            // O(N) with the dynamic array resizing
            game.wrongGuesses.add(letter)
        }


        return game
    }

}