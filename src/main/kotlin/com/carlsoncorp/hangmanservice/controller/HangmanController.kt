package com.carlsoncorp.hangmanservice.controller

import com.carlsoncorp.hangmanservice.controller.model.Context
import com.carlsoncorp.hangmanservice.model.Game
import com.carlsoncorp.hangmanservice.model.GameState
import com.carlsoncorp.hangmanservice.service.HangmanService
import com.carlsoncorp.hangmanservice.service.exception.DuplicateWrongGuessException
import com.carlsoncorp.hangmanservice.service.exception.GameAlreadyOverException
import com.carlsoncorp.hangmanservice.service.exception.GameNotFoundException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@Controller
class HangmanController(private val hangmanService: HangmanService) {

    @PostMapping(path = ["/games"], produces = ["application/json"] )
    @ResponseBody
    fun newGame(@RequestBody(required = false) maxNumberOfGuesses: Int?,
                @RequestBody(required = false) secretWord: String?,
                @RequestBody(required = true) context: Context
    ): Game {
        // could do some validation to make sure secretWord fits the locale
        return hangmanService.createNewGame(maxNumberOfGuesses, secretWord)
    }

    @GetMapping(path = ["/games/{id}"], produces = ["application/json"] )
    @ResponseBody
    fun getGameById(@PathVariable("id") id: String): Game {
        //TODO: how to pass the Context to get API
        try {
            val game = hangmanService.getGame(id)
            return game
        } catch(gmfe: GameNotFoundException) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find the game.")
        }
    }

    @PostMapping(path = ["/games/{id}/guess"], consumes = ["application/json"], produces = ["application/json"] )
    @ResponseBody
    fun guess(@PathVariable("id") gameId: String,
              @RequestBody(required = true) letter: String,
              @RequestBody(required = true) context: Context): Game {

        // Guesses don't matter if it's lower or upper case. No need to null check since it's required field. TODO: double check this!
        var lowerCaseLetter = letter.toLowerCase()

        // Validate the guess
        if (lowerCaseLetter.isNullOrBlank() || lowerCaseLetter.length > 1 || lowerCaseLetter[0] in 'b'..'z') {
            throw ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Guess is not a letter.")
        }

        val game: Game

        try {
            game = hangmanService.guess(letter[0], gameId, context.sessionId)
        } catch(gmfe: GameNotFoundException) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find the game.")
        } catch(dwge: DuplicateWrongGuessException) {
            throw ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Duplicate wrong guess, try a different letter.")
        } catch(gaoe: GameAlreadyOverException) {
            // TODO: figure out how to let the client know the game was won vs failed - they may want to customize a msg.
            throw ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Game is already completed. ")
        }
        //TODO: could prob use Spring exception handlers but for now doing it here

        return game
    }

}