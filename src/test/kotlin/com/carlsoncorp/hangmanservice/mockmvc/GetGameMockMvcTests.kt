package com.carlsoncorp.hangmanservice.mockmvc

import com.carlsoncorp.hangmanservice.controller.model.Game
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
class GetGameMockMvcTests {

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

    @Test
    @Throws(Exception::class)
    fun testGetGameById_samePlayerAsCreated() {

        val sessionId = "123"
        var maxNumberOGuesses = 2

        var id = createGame(sessionId, maxNumberOGuesses)

        mockMvc!!.perform(get("/games/${id}")
            .header("x-session-id", sessionId))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id", not(emptyString())))
            .andExpect(jsonPath("$.gameState", equalTo("NEW_GAME")))
            .andExpect(jsonPath("$.playerTurn", equalTo(sessionId)))
            .andExpect(jsonPath("$.numGuessesLeft", equalTo(maxNumberOGuesses)))
            .andExpect(jsonPath("$.wrongGuesses", equalTo("")))
            .andExpect(jsonPath("$.guessingWordTracker", equalTo("**")))
            .andDo(print())
    }

    @Test
    @Throws(Exception::class)
    fun testGetGameById_newPlayerAdded() {

        val sessionId1 = "123"
        val sessionId2 = "456"
        var maxNumberOGuesses = 2

        var id = createGame(sessionId1, maxNumberOGuesses)

        mockMvc!!.perform(get("/games/${id}")
            .header("x-session-id", sessionId2))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id", not(emptyString())))
            .andExpect(jsonPath("$.gameState", equalTo("NEW_GAME")))
            .andExpect(jsonPath("$.playerTurn", equalTo(sessionId1)))
            .andExpect(jsonPath("$.numGuessesLeft", equalTo(maxNumberOGuesses)))
            .andExpect(jsonPath("$.wrongGuesses", equalTo("")))
            .andExpect(jsonPath("$.guessingWordTracker", equalTo("**")))
            .andDo(print())
    }

    @Test
    @Throws(Exception::class)
    fun test404_GameNotFound() {
        mockMvc!!.perform(get("/games/123")
            .header("x-session-id", "123"))
            .andExpect(status().isNotFound)
            .andDo(print())
    }

}