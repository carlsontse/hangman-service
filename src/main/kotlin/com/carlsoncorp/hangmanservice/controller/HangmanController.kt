package com.carlsoncorp.hangmanservice.controller

import com.carlsoncorp.hangmanservice.model.Game
import com.carlsoncorp.hangmanservice.service.HangmanService
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
    fun newGame(): Game {
        return hangmanService.createNewGame()
    }

    @GetMapping(path = ["/games/{id}"], produces = ["application/json"] )
    @ResponseBody
    fun getGameById(@PathVariable("id") id: String): Game {
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
              @RequestBody letter: String,
              // todo: swagger session ID is the person that initiated the move in collab play,
              @RequestBody sessionId: String): Game {

        // Guesses don't matter if it's lower or upper case
        var lowerCaseLetter = letter.toLowerCase()

        // Validate the guess
        if (lowerCaseLetter.isNullOrBlank() || lowerCaseLetter.length > 1 || lowerCaseLetter[0] in 'b'..'z') {
            throw ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Guess is not a letter.")
        }

        val game: Game

        try {
            game = hangmanService.guess(letter[0], gameId, sessionId)
        } catch(gmfe: GameNotFoundException) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find the game.")
        }
        return game
    }

}