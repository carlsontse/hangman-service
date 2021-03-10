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
import org.hamcrest.Matchers.`is` as Is

@RunWith(SpringRunner::class)
@SpringBootTest
@AutoConfigureMockMvc
class GetGamesMockMvcTests {

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
/** TODO: add this back when i implement delete, teh state has changed across tests!!
    @Test
    @Throws(Exception::class)
    fun testGetGames_defaults_expectAllGamesReturned() {

        val sessionId1 = "123"
        val sessionId2 = "456"
        var maxNumberOGuesses = 2

        createGame(sessionId1, maxNumberOGuesses)
        createGame(sessionId2, maxNumberOGuesses)

        mockMvc!!.perform(get("/games")
            .header("x-session-id", sessionId1))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()", Is(2)))
            .andExpect(jsonPath("$.[0].id", not(emptyString())))
            .andExpect(jsonPath("$.[0].gameState", equalTo("NEW_GAME")))
            .andExpect(jsonPath("$.[0].playerTurn", equalTo(sessionId1)))
            .andExpect(jsonPath("$.[0].numGuessesLeft", equalTo(maxNumberOGuesses)))
            .andExpect(jsonPath("$.[0].wrongGuesses", equalTo("")))
            .andExpect(jsonPath("$.[0].guessingWordTracker", equalTo("**")))
            .andExpect(jsonPath("$.[1].id", not(emptyString())))
            .andExpect(jsonPath("$.[1].gameState", equalTo("NEW_GAME")))
            .andExpect(jsonPath("$.[1].playerTurn", equalTo(sessionId2)))
            .andExpect(jsonPath("$.[1].numGuessesLeft", equalTo(maxNumberOGuesses)))
            .andExpect(jsonPath("$.[1].wrongGuesses", equalTo("")))
            .andExpect(jsonPath("$.[1].guessingWordTracker", equalTo("**")))
            .andDo(print())
    }
    **/
/*
    @Test
    @Throws(Exception::class)
    fun testGetGames_returnOnlyActiveGame() {

        val sessionId1 = "123"
        val sessionId2 = "456"
        var maxNumberOGuesses = 1

        val id = createGame(sessionId1, maxNumberOGuesses)

        var str: String

        val guessRequest = GuessRequest()
        guessRequest.letter = "c"
        str = ObjectMapper().writeValueAsString(guessRequest)

        // Perform a guess w/ incorrect letter to end the game
        mockMvc!!.perform(put("/games/${id}/guess")
            .contentType(MediaType.APPLICATION_JSON)
            .content(str)
            .header("x-session-id", sessionId1))
            .andDo(print())

        createGame(sessionId2, maxNumberOGuesses)

        mockMvc!!.perform(get("/games").param("is_active", "true")
            .header("x-session-id", sessionId1))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()", Is(1)))
            .andExpect(jsonPath("$.[0].id", not(emptyString())))
            .andExpect(jsonPath("$.[0].gameState", equalTo("NEW_GAME")))
            .andExpect(jsonPath("$.[0].playerTurn", equalTo(sessionId2)))
            .andExpect(jsonPath("$.[0].numGuessesLeft", equalTo(maxNumberOGuesses)))
            .andExpect(jsonPath("$.[0].wrongGuesses", equalTo("")))
            .andExpect(jsonPath("$.[0].guessingWordTracker", equalTo("**")))
            .andDo(print())
    }*/

}