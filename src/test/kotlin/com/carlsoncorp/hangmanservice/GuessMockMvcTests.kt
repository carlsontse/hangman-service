package com.carlsoncorp.hangmanservice

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
import org.hamcrest.Matchers.`is` as Is

@RunWith(SpringRunner::class)
@SpringBootTest
@AutoConfigureMockMvc
class GuessMockMvcTests {

    @Autowired
    private val mockMvc: MockMvc? = null

    private val SECRET_WORD = "ha"
    private val MAX_NUMBER_OF_GUESSES = 1

    /**
     * Create the game
     * @param sessionId player to create game as
     * @return String id of the game
     */
    private fun createGame(sessionId: String): String {
        var str: String

        val newGameRequest = NewGameRequest()
        newGameRequest.secretWord = SECRET_WORD
        newGameRequest.maxNumberOfGuesses = MAX_NUMBER_OF_GUESSES

        try {
            str = ObjectMapper().writeValueAsString(newGameRequest)
        } catch (e:Exception) {
            throw RuntimeException(e)
        }

        val sessionId = "123"

        // Create the game first
        val result = mockMvc!!.perform(post("/games")
            .contentType(MediaType.APPLICATION_JSON)
            .content(str)
            .header("x-session-id", sessionId))
            .andExpect(status().isOk)
            .andReturn()

        var game = ObjectMapper().readValue(result.response.contentAsString, Game::class.java)

        return game.id!!
    }

    @Test
    @Throws(Exception::class)
    fun testSinglePlayerGame_expectWin() {

        val sessionId = "123"
        var numGuessesLeftTracker = MAX_NUMBER_OF_GUESSES
        var wrongGuessesTracker = ""
        var guessingWordTracker = "**"

        var id = createGame(sessionId)

        var str: String

        val guessRequest = GuessRequest()
        guessRequest.letter = "a"
        str = ObjectMapper().writeValueAsString(guessRequest)

        // Perform a guess w/ correct letter
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


    /**mockMvc!!.perform(get("/games").param("name", "Joe"))
    .andExpect(status().isOk)
    .andExpect(model().attribute("msg", "Hi there, Joe."))
    .andExpect(view().name("hello-page"))
    .andDo(print())*/
}