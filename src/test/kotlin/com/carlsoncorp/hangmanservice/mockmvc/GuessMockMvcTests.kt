package com.carlsoncorp.hangmanservice.mockmvc

import com.carlsoncorp.hangmanservice.controller.model.Game
import com.carlsoncorp.hangmanservice.controller.model.GuessRequest
import com.carlsoncorp.hangmanservice.controller.model.NewGameRequest
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.*
import org.hamcrest.Matchers.*

@RunWith(SpringRunner::class)
@SpringBootTest
@AutoConfigureMockMvc
class GuessMockMvcTests {

    @Autowired
    private val mockMvc: MockMvc? = null

    private val SECRET_WORD = "ha"

    /**
     * Create the game
     * @param sessionId player to create game as
     * @return String id of the game
     */
    private fun createGame(sessionId: String, maxNumberOGuesses: Int): String {
        var str: String

        val newGameRequest = NewGameRequest()
        newGameRequest.secretWord = SECRET_WORD
        newGameRequest.maxNumberOfGuesses = maxNumberOGuesses

        try {
            str = ObjectMapper().writeValueAsString(newGameRequest)
        } catch (e:Exception) {
            throw RuntimeException(e)
        }

        // Create the game first
        val result = mockMvc!!.perform(post("/games")
            .contentType(MediaType.APPLICATION_JSON)
            .content(str)
            .header("x-session-id", sessionId))
            .andExpect(status().isOk)
            .andDo(print())
            .andReturn()

        var game = ObjectMapper().readValue(result.response.contentAsString, Game::class.java)

        return game.id!!
    }

    /**
     * Get the game, adds the player
     * @param sessionId player to add to game
     */
    private fun getGame(gameId: String, sessionId: String) {

        // Create the game first
        mockMvc!!.perform(get("/games/${gameId}")
            .header("x-session-id", sessionId))
            .andExpect(status().isOk)
            .andDo(print())
    }

    @Test
    @Throws(Exception::class)
    fun testSinglePlayerGame_expectWin() {

        val sessionId = "123"
        var maxNumberOGuesses = 2
        var numGuessesLeftTracker = maxNumberOGuesses
        var wrongGuessesTracker = ""
        var guessingWordTracker = "**"

        var id = createGame(sessionId, maxNumberOGuesses)

        var str: String

        val guessRequest = GuessRequest()
        guessRequest.letter = "a"
        str = ObjectMapper().writeValueAsString(guessRequest)

        // 1. Perform a guess w/ correct letter
        guessingWordTracker = "*a"
        mockMvc!!.perform(put("/games/${id}/guess")
            .contentType(MediaType.APPLICATION_JSON)
            .content(str)
            .header("x-session-id", sessionId))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id", not(emptyString())))
            .andExpect(jsonPath("$.gameState", equalTo("NEW_GAME")))
            .andExpect(jsonPath("$.playerTurn", equalTo(sessionId)))
            .andExpect(jsonPath("$.numGuessesLeft", equalTo(numGuessesLeftTracker)))
            .andExpect(jsonPath("$.wrongGuesses", equalTo(wrongGuessesTracker)))
            .andExpect(jsonPath("$.guessingWordTracker", equalTo(guessingWordTracker)))
            .andDo(print())

        guessRequest.letter = "c"
        str = ObjectMapper().writeValueAsString(guessRequest)

        // Perform a guess w/ incorrect letter
        wrongGuessesTracker+= guessRequest.letter
        mockMvc!!.perform(put("/games/${id}/guess")
            .contentType(MediaType.APPLICATION_JSON)
            .content(str)
            .header("x-session-id", sessionId))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id", not(emptyString())))
            .andExpect(jsonPath("$.gameState", equalTo("NEW_GAME")))
            .andExpect(jsonPath("$.playerTurn", equalTo(sessionId)))
            .andExpect(jsonPath("$.numGuessesLeft", equalTo(--numGuessesLeftTracker)))
            .andExpect(jsonPath("$.wrongGuesses", equalTo(wrongGuessesTracker)))
            .andExpect(jsonPath("$.guessingWordTracker", equalTo(guessingWordTracker)))
            .andDo(print())

        guessRequest.letter = "h"
        str = ObjectMapper().writeValueAsString(guessRequest)

        // Perform a guess w/ correct letter and end the game
        guessingWordTracker = "ha"
        mockMvc!!.perform(put("/games/${id}/guess")
            .contentType(MediaType.APPLICATION_JSON)
            .content(str)
            .header("x-session-id", sessionId))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id", not(emptyString())))
            .andExpect(jsonPath("$.gameState", equalTo("GAME_OVER_WIN")))
            .andExpect(jsonPath("$.playerTurn", equalTo(sessionId)))
            .andExpect(jsonPath("$.numGuessesLeft", equalTo(numGuessesLeftTracker)))
            .andExpect(jsonPath("$.wrongGuesses", equalTo(wrongGuessesTracker)))
            .andExpect(jsonPath("$.guessingWordTracker", equalTo(guessingWordTracker)))
            .andDo(print())
    }

    @Test
    @Throws(Exception::class)
    fun testSinglePlayerGame_expectLoss() {

        val sessionId = "123"
        var maxNumberOGuesses = 1
        var numGuessesLeftTracker = maxNumberOGuesses
        var wrongGuessesTracker = ""
        var guessingWordTracker = "**"

        var id = createGame(sessionId, maxNumberOGuesses)

        var str: String

        val guessRequest = GuessRequest()
        guessRequest.letter = "a"
        str = ObjectMapper().writeValueAsString(guessRequest)

        // 1. Perform a guess w/ correct letter
        guessingWordTracker = "*a"
        mockMvc!!.perform(put("/games/${id}/guess")
            .contentType(MediaType.APPLICATION_JSON)
            .content(str)
            .header("x-session-id", sessionId))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id", not(emptyString())))
            .andExpect(jsonPath("$.gameState", equalTo("NEW_GAME")))
            .andExpect(jsonPath("$.playerTurn", equalTo(sessionId)))
            .andExpect(jsonPath("$.numGuessesLeft", equalTo(numGuessesLeftTracker)))
            .andExpect(jsonPath("$.wrongGuesses", equalTo(wrongGuessesTracker)))
            .andExpect(jsonPath("$.guessingWordTracker", equalTo(guessingWordTracker)))
            .andDo(print())

        guessRequest.letter = "c"
        str = ObjectMapper().writeValueAsString(guessRequest)

        // Perform a guess w/ incorrect letter
        wrongGuessesTracker+= guessRequest.letter
        mockMvc!!.perform(put("/games/${id}/guess")
            .contentType(MediaType.APPLICATION_JSON)
            .content(str)
            .header("x-session-id", sessionId))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id", not(emptyString())))
            .andExpect(jsonPath("$.gameState", equalTo("GAME_OVER_LOSS")))
            .andExpect(jsonPath("$.playerTurn", equalTo(sessionId)))
            .andExpect(jsonPath("$.numGuessesLeft", equalTo(--numGuessesLeftTracker)))
            .andExpect(jsonPath("$.wrongGuesses", equalTo(wrongGuessesTracker)))
            .andExpect(jsonPath("$.guessingWordTracker", equalTo(guessingWordTracker)))
            .andDo(print())
    }

    @Test
    @Throws(Exception::class)
    fun testTwoPlayerGame_expectWin() {

        val sessionId1 = "123"
        val sessionId2 = "456"
        var maxNumberOGuesses = 2
        var numGuessesLeftTracker = maxNumberOGuesses
        var wrongGuessesTracker = ""
        var guessingWordTracker = "**"

        var id = createGame(sessionId1, maxNumberOGuesses)

        getGame(id, sessionId2)

        var str: String

        val guessRequest = GuessRequest()
        guessRequest.letter = "a"
        str = ObjectMapper().writeValueAsString(guessRequest)

        // Player 1: Perform a guess w/ correct letter
        guessingWordTracker = "*a"
        mockMvc!!.perform(put("/games/${id}/guess")
            .contentType(MediaType.APPLICATION_JSON)
            .content(str)
            .header("x-session-id", sessionId1))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id", not(emptyString())))
            .andExpect(jsonPath("$.gameState", equalTo("NEW_GAME")))
            .andExpect(jsonPath("$.playerTurn", equalTo(sessionId2)))
            .andExpect(jsonPath("$.numGuessesLeft", equalTo(numGuessesLeftTracker)))
            .andExpect(jsonPath("$.wrongGuesses", equalTo(wrongGuessesTracker)))
            .andExpect(jsonPath("$.guessingWordTracker", equalTo(guessingWordTracker)))
            .andDo(print())

        guessRequest.letter = "c"
        str = ObjectMapper().writeValueAsString(guessRequest)

        // Player 2: Perform a guess w/ incorrect letter
        wrongGuessesTracker+= guessRequest.letter
        mockMvc!!.perform(put("/games/${id}/guess")
            .contentType(MediaType.APPLICATION_JSON)
            .content(str)
            .header("x-session-id", sessionId2))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id", not(emptyString())))
            .andExpect(jsonPath("$.gameState", equalTo("NEW_GAME")))
            .andExpect(jsonPath("$.playerTurn", equalTo(sessionId1)))
            .andExpect(jsonPath("$.numGuessesLeft", equalTo(--numGuessesLeftTracker)))
            .andExpect(jsonPath("$.wrongGuesses", equalTo(wrongGuessesTracker)))
            .andExpect(jsonPath("$.guessingWordTracker", equalTo(guessingWordTracker)))
            .andDo(print())

        guessRequest.letter = "h"
        str = ObjectMapper().writeValueAsString(guessRequest)

        // Player 1: Perform a guess w/ correct letter and end the game
        guessingWordTracker = "ha"
        mockMvc!!.perform(put("/games/${id}/guess")
            .contentType(MediaType.APPLICATION_JSON)
            .content(str)
            .header("x-session-id", sessionId1))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id", not(emptyString())))
            .andExpect(jsonPath("$.gameState", equalTo("GAME_OVER_WIN")))
            .andExpect(jsonPath("$.playerTurn", equalTo(sessionId2)))
            .andExpect(jsonPath("$.numGuessesLeft", equalTo(numGuessesLeftTracker)))
            .andExpect(jsonPath("$.wrongGuesses", equalTo(wrongGuessesTracker)))
            .andExpect(jsonPath("$.guessingWordTracker", equalTo(guessingWordTracker)))
            .andDo(print())
    }

    @Test
    @Throws(Exception::class)
    fun testTwoPlayerGame_expectLoss() {

        val sessionId1 = "123"
        val sessionId2 = "456"
        var maxNumberOGuesses = 2
        var numGuessesLeftTracker = maxNumberOGuesses
        var wrongGuessesTracker = ""
        var guessingWordTracker = "**"

        var id = createGame(sessionId1, maxNumberOGuesses)

        getGame(id, sessionId2)

        var str: String

        val guessRequest = GuessRequest()
        guessRequest.letter = "a"
        str = ObjectMapper().writeValueAsString(guessRequest)

        // Player 1: Perform a guess w/ correct letter
        guessingWordTracker = "*a"
        mockMvc!!.perform(put("/games/${id}/guess")
            .contentType(MediaType.APPLICATION_JSON)
            .content(str)
            .header("x-session-id", sessionId1))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id", not(emptyString())))
            .andExpect(jsonPath("$.gameState", equalTo("NEW_GAME")))
            .andExpect(jsonPath("$.playerTurn", equalTo(sessionId2)))
            .andExpect(jsonPath("$.numGuessesLeft", equalTo(numGuessesLeftTracker)))
            .andExpect(jsonPath("$.wrongGuesses", equalTo(wrongGuessesTracker)))
            .andExpect(jsonPath("$.guessingWordTracker", equalTo(guessingWordTracker)))
            .andDo(print())

        guessRequest.letter = "c"
        str = ObjectMapper().writeValueAsString(guessRequest)

        // Player 2: Perform a guess w/ incorrect letter
        wrongGuessesTracker+= guessRequest.letter
        mockMvc!!.perform(put("/games/${id}/guess")
            .contentType(MediaType.APPLICATION_JSON)
            .content(str)
            .header("x-session-id", sessionId2))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id", not(emptyString())))
            .andExpect(jsonPath("$.gameState", equalTo("NEW_GAME")))
            .andExpect(jsonPath("$.playerTurn", equalTo(sessionId1)))
            .andExpect(jsonPath("$.numGuessesLeft", equalTo(--numGuessesLeftTracker)))
            .andExpect(jsonPath("$.wrongGuesses", equalTo(wrongGuessesTracker)))
            .andExpect(jsonPath("$.guessingWordTracker", equalTo(guessingWordTracker)))
            .andDo(print())

        guessRequest.letter = "z"
        str = ObjectMapper().writeValueAsString(guessRequest)

        // Player 1: Perform a guess w/ incorrect letter and end the game
        wrongGuessesTracker+= guessRequest.letter
        mockMvc!!.perform(put("/games/${id}/guess")
            .contentType(MediaType.APPLICATION_JSON)
            .content(str)
            .header("x-session-id", sessionId1))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id", not(emptyString())))
            .andExpect(jsonPath("$.gameState", equalTo("GAME_OVER_LOSS")))
            .andExpect(jsonPath("$.playerTurn", equalTo(sessionId2)))
            .andExpect(jsonPath("$.numGuessesLeft", equalTo(--numGuessesLeftTracker)))
            .andExpect(jsonPath("$.wrongGuesses", equalTo(wrongGuessesTracker)))
            .andExpect(jsonPath("$.guessingWordTracker", equalTo(guessingWordTracker)))
            .andDo(print())
    }

    /**
        Error scenarios
     */
    @Test
    @Throws(Exception::class)
    fun test404_GameNotFound() {
        var str: String

        val guessRequest = GuessRequest()
        guessRequest.letter = "a"
        str = ObjectMapper().writeValueAsString(guessRequest)

        // 1. Perform a guess w/ correct letter
        mockMvc!!.perform(put("/games/123/guess")
            .contentType(MediaType.APPLICATION_JSON)
            .content(str)
            .header("x-session-id", "123"))
            .andExpect(status().isNotFound)
            .andDo(print())
    }

    @Test
    @Throws(Exception::class)
    fun test406_GameIsAlreadyOver() {
        val sessionId = "123"
        var maxNumberOGuesses = 1
        var numGuessesLeftTracker = maxNumberOGuesses
        var wrongGuessesTracker = ""
        var guessingWordTracker = "**"

        var id = createGame(sessionId, maxNumberOGuesses)

        var str: String

        val guessRequest = GuessRequest()
        guessRequest.letter = "c"
        str = ObjectMapper().writeValueAsString(guessRequest)

        // Perform a guess w/ incorrect letter
        wrongGuessesTracker+= guessRequest.letter
        mockMvc!!.perform(put("/games/${id}/guess")
            .contentType(MediaType.APPLICATION_JSON)
            .content(str)
            .header("x-session-id", sessionId))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id", not(emptyString())))
            .andExpect(jsonPath("$.gameState", equalTo("GAME_OVER_LOSS")))
            .andExpect(jsonPath("$.playerTurn", equalTo(sessionId)))
            .andExpect(jsonPath("$.numGuessesLeft", equalTo(--numGuessesLeftTracker)))
            .andExpect(jsonPath("$.wrongGuesses", equalTo(wrongGuessesTracker)))
            .andExpect(jsonPath("$.guessingWordTracker", equalTo(guessingWordTracker)))
            .andDo(print())

        guessRequest.letter = "a"
        str = ObjectMapper().writeValueAsString(guessRequest)

        // Perform a guess w/ correct letter
        mockMvc!!.perform(put("/games/${id}/guess")
            .contentType(MediaType.APPLICATION_JSON)
            .content(str)
            .header("x-session-id", sessionId))
            .andExpect(status().isNotAcceptable)
            .andDo(print())
    }

    @Test
    @Throws(Exception::class)
    fun test422_GuessIsNotLetter() {
        val sessionId = "123"
        var maxNumberOGuesses = 1

        var id = createGame(sessionId, maxNumberOGuesses)

        var str: String

        val guessRequest = GuessRequest()
        guessRequest.letter = "&"
        str = ObjectMapper().writeValueAsString(guessRequest)

        // Perform a guess w/ correct letter
        mockMvc!!.perform(put("/games/${id}/guess")
            .contentType(MediaType.APPLICATION_JSON)
            .content(str)
            .header("x-session-id", sessionId))
            .andExpect(status().isUnprocessableEntity)
            .andDo(print())
    }

    @Test
    @Throws(Exception::class)
    fun test422_DuplicateIncorrectGuess() {
        val sessionId = "123"
        var maxNumberOGuesses = 2
        var numGuessesLeftTracker = maxNumberOGuesses
        var wrongGuessesTracker = ""
        var guessingWordTracker = "**"

        var id = createGame(sessionId, maxNumberOGuesses)

        var str: String

        val guessRequest = GuessRequest()
        guessRequest.letter = "c"
        str = ObjectMapper().writeValueAsString(guessRequest)

        // Perform a guess w/ incorrect letter
        wrongGuessesTracker+= guessRequest.letter
        mockMvc!!.perform(put("/games/${id}/guess")
            .contentType(MediaType.APPLICATION_JSON)
            .content(str)
            .header("x-session-id", sessionId))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id", not(emptyString())))
            .andExpect(jsonPath("$.gameState", equalTo("NEW_GAME")))
            .andExpect(jsonPath("$.playerTurn", equalTo(sessionId)))
            .andExpect(jsonPath("$.numGuessesLeft", equalTo(--numGuessesLeftTracker)))
            .andExpect(jsonPath("$.wrongGuesses", equalTo(wrongGuessesTracker)))
            .andExpect(jsonPath("$.guessingWordTracker", equalTo(guessingWordTracker)))
            .andDo(print())

        mockMvc!!.perform(put("/games/${id}/guess")
            .contentType(MediaType.APPLICATION_JSON)
            .content(str)
            .header("x-session-id", sessionId))
            .andExpect(status().isUnprocessableEntity)
            .andDo(print())
    }

    @Test
    @Throws(Exception::class)
    fun test403_NotPlayerTurn() {
        val sessionId = "123"
        var maxNumberOGuesses = 1
        var wrongGuessesTracker = ""

        var id = createGame(sessionId, maxNumberOGuesses)

        var str: String

        val guessRequest = GuessRequest()
        guessRequest.letter = "c"
        str = ObjectMapper().writeValueAsString(guessRequest)

        // Perform a guess w/ incorrect letter
        wrongGuessesTracker+= guessRequest.letter
        mockMvc!!.perform(put("/games/${id}/guess")
            .contentType(MediaType.APPLICATION_JSON)
            .content(str)
            .header("x-session-id", "456"))
            .andExpect(status().isForbidden)
            .andDo(print())
    }

}