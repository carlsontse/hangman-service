package com.carlsoncorp.hangmanservice

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
class NewGameMockMvcTests {

	@Autowired
	private val mockMvc: MockMvc? = null

	@Test
	@Throws(Exception::class)
	fun testDefaultNewGame_expectDefaultValues() {

		var str: String

		val newGameRequest = NewGameRequest()

		try {
			str = ObjectMapper().writeValueAsString(newGameRequest)
		} catch (e:Exception) {
			throw RuntimeException(e)
		}

		val sessionId = "123"

		mockMvc!!.perform(post("/games")
				.contentType(MediaType.APPLICATION_JSON)
				.content(str)
				.header("x-session-id", sessionId))
			.andExpect(status().isOk)
			.andExpect(jsonPath("$.id", not(emptyString())))
			.andExpect(jsonPath("$.gameState", equalTo("NEW_GAME")))
			.andExpect(jsonPath("$.playerTurn", equalTo(sessionId)))
			.andExpect(jsonPath("$.numGuessesLeft", greaterThan(0)))
			.andExpect(jsonPath("$.wrongGuesses", Is(emptyString())))
			.andExpect(jsonPath("$.guessingWordTracker", not(emptyString())))
			.andDo(print())
	}

	@Test
	@Throws(Exception::class)
	fun testNewGameWithAllValuesSet() {
		var str: String

		val newGameRequest = NewGameRequest()
		newGameRequest.maxNumberOfGuesses = 2
		newGameRequest.secretWord = "hi"

		try {
			str = ObjectMapper().writeValueAsString(newGameRequest)
		} catch (e:Exception) {
			throw RuntimeException(e)
		}

		val sessionId = "123"

		mockMvc!!.perform(post("/games")
			.contentType(MediaType.APPLICATION_JSON)
			.content(str)
			.header("x-session-id", sessionId))
			.andExpect(status().isOk)
			.andExpect(jsonPath("$.id", not(emptyString())))
			.andExpect(jsonPath("$.gameState", equalTo("NEW_GAME")))
			.andExpect(jsonPath("$.playerTurn", equalTo(sessionId)))
			.andExpect(jsonPath("$.numGuessesLeft", equalTo(newGameRequest.maxNumberOfGuesses)))
			.andExpect(jsonPath("$.wrongGuesses", Is(emptyString())))
			.andExpect(jsonPath("$.guessingWordTracker", equalTo("**")))
			.andDo(print())
	}


	/**mockMvc!!.perform(get("/games").param("name", "Joe"))
	.andExpect(status().isOk)
	.andExpect(model().attribute("msg", "Hi there, Joe."))
	.andExpect(view().name("hello-page"))
	.andDo(print())*/
}