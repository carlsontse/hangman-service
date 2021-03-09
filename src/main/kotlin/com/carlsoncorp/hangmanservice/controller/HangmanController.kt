package com.carlsoncorp.hangmanservice.controller

import com.carlsoncorp.hangmanservice.controller.model.GuessRequest
import com.carlsoncorp.hangmanservice.controller.model.NewGameRequest
import com.carlsoncorp.hangmanservice.service.HangmanService
import com.carlsoncorp.hangmanservice.service.exception.DuplicateWrongGuessException
import com.carlsoncorp.hangmanservice.service.exception.GameAlreadyOverException
import com.carlsoncorp.hangmanservice.service.exception.GameNotFoundException
import com.carlsoncorp.hangmanservice.service.exception.NotPlayerTurnException
import io.swagger.annotations.*
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@Controller
@Api(description = "Set of endpoints for Creating, Getting and Playing games of Hangman.")
//TODO: We can also add other x- headers for device, browser, etc for analytics.
class HangmanController(private val hangmanService: HangmanService) {

    @PostMapping(path = ["/games"], produces = ["application/json"] )
    @ResponseBody
    @ApiOperation("Returns list of all Games in the system.")
    @ApiResponses(
        ApiResponse( code = 200, message = "OK"),
        ApiResponse( code = 400, message = "Bad Request"),
        ApiResponse( code = 422, message = "Invalid new game request")
    )
    fun newGame(@ApiParam("Unique session id to identify a player creating the game.", required = true)
                    @RequestHeader("x-session-id", required = true) sessionId: String,
                @RequestBody(required = false) newGameRequest: NewGameRequest)
                                            : com.carlsoncorp.hangmanservice.controller.model.Game {
        // could do some validation to make sure secretWord fits the locale

        if (newGameRequest.maxNumberOfGuesses != null && newGameRequest.maxNumberOfGuesses!! <= 0) {
            throw ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid max number of guesses.")
        }

        // If the secret word was set but it's empty or full of white characters
        if (newGameRequest.secretWord?.isBlank() == true) {
            throw ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid secret word.")
        }

        val game = hangmanService.createNewGame(sessionId, newGameRequest.maxNumberOfGuesses,
            newGameRequest.secretWord?.trim())

        return mapInternalGameToExternal(game)
    }

    @GetMapping(path = ["/games"], produces = ["application/json"] )
    @ResponseBody
    @ApiOperation("Returns list of all Games in the system.")
    @ApiResponses(
        ApiResponse( code = 200, message = "OK")
    )
    fun getGames(@ApiParam("Filter only active games.")
                    @RequestParam("is_active", required = false, defaultValue = "false") isActive: Boolean)
                                                    : List<com.carlsoncorp.hangmanservice.controller.model.Game> {
        //TODO: how to pass the Context to get API
        val games = hangmanService.getGames(isActive)
        return games.map { mapInternalGameToExternal(it) }
    }

    @GetMapping(path = ["/games/{id}"], produces = ["application/json"] )
    @ResponseBody
    @ApiOperation("Returns a single game by its identifier.")
    @ApiResponses(
        ApiResponse( code = 200, message = "OK"),
        ApiResponse( code = 400, message = "Bad Request"),
        ApiResponse( code = 404, message = "Game not found")
    )
    fun getGameById(@ApiParam("Unique session id to identify a player entering the game")
                        @RequestHeader("x-session-id") sessionId: String,
                    @ApiParam("Unique game identifier as a guid")
                        @PathVariable("id") id: String): com.carlsoncorp.hangmanservice.controller.model.Game {
        try {
            val game = hangmanService.getGame(id, sessionId)
            return mapInternalGameToExternal(game)
        } catch(gmfe: GameNotFoundException) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find the game.")
        }
    }

    // 'Guessing' is a PUT to me because it's an idempotent operation.
    @PutMapping(path = ["/games/{id}/guess"], consumes = ["application/json"], produces = ["application/json"] )
    @ResponseBody
    @ApiOperation("Perform a guess on the specified Hangman game.")
    @ApiResponses(
        ApiResponse( code = 200, message = "OK"),
        ApiResponse( code = 400, message = "Bad Request"),
        ApiResponse( code = 403, message = "Not player turn"),
        ApiResponse( code = 404, message = "Game not found"),
        ApiResponse( code = 406, message = "Game is already over"),
        ApiResponse( code = 422, message = "Invalid guess")
    )
    fun guess(@ApiParam("Unique identifier for a game", required = true)
                @PathVariable("id", required = true) gameId: String,
              @ApiParam("Unique session id to identify a player making the guess.", required = true)
                @RequestHeader("x-session-id", required = true) sessionId: String,
              @RequestBody(required = true) guessRequest: GuessRequest): com.carlsoncorp.hangmanservice.controller.model.Game {

        // Guesses don't matter if it's lower or upper case. No need to null check since it's required field. TODO: double check this!
        var lowerCaseLetter = guessRequest.letter?.toLowerCase()

        // Validate the guess, this would change when locale needs to be supported.
        if (lowerCaseLetter.isNullOrBlank() || lowerCaseLetter.length > 1 || lowerCaseLetter[0] !in 'a'..'z') {
            throw ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Guess is not a letter.")
        }

        try {
            val game = hangmanService.guess(lowerCaseLetter[0], gameId, sessionId)
            return mapInternalGameToExternal(game)
        } catch(gmfe: GameNotFoundException) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find the game.")
        } catch(dwge: DuplicateWrongGuessException) {
            throw ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Duplicate wrong guess, try a different letter.")
        } catch(gaoe: GameAlreadyOverException) {
            // TODO: figure out how to let the client know the game was won vs failed - they may want to customize a msg.
            throw ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Game is already completed.")
        } catch(npte: NotPlayerTurnException) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Not the player turn.")
        }
        //TODO: could prob use Spring exception handlers but for now doing it here
    }

    /**
     * Map the internal Game representation to the external Game representation.
     * @param game Internal representation of the game of Hangman.
     * @return Game External representation of the game of Hangman, contains everything the client needs to know.
     */
    private fun mapInternalGameToExternal(game: com.carlsoncorp.hangmanservice.model.Game): com.carlsoncorp.hangmanservice.controller.model.Game {
        val externalGame = com.carlsoncorp.hangmanservice.controller.model.Game()
        externalGame.id = game.getId()
        externalGame.guessingWordTracker = game.getGuessingWordTracker()
        externalGame.wrongGuesses = game.getWrongGuesses().map { it.guessLetter }.toCharArray() // Only return the wrong guess letter back
        externalGame.gameState = game.getState().toString()
        externalGame.numGuessesLeft = game.getNumberOfRemainingGuesses()
        externalGame.playerTurn = game.getNextPlayer()

        return externalGame
    }

}