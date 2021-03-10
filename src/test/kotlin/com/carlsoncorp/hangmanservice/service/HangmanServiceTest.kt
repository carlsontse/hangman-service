package com.carlsoncorp.hangmanservice.service

import com.carlsoncorp.hangmanservice.service.exception.DuplicateWrongGuessException
import com.carlsoncorp.hangmanservice.service.exception.GameAlreadyOverException
import com.carlsoncorp.hangmanservice.service.exception.GameNotFoundException
import com.carlsoncorp.hangmanservice.service.exception.NotPlayerTurnException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach

internal class HangmanServiceTest {

    lateinit var hangmanService: HangmanService

    @BeforeEach
    fun before() {
        hangmanService = HangmanService()
    }

    @Test
    fun createNewGame_defaultValues() {
        val sessionID1 = "123"

        val game = hangmanService.createNewGame(sessionID1, null, null)

        assertEquals(1, hangmanService.getGames(true).size)
    }

    @Test
    fun createNewGame_setValues() {
        val sessionID1 = "123"

        val game = hangmanService.createNewGame(sessionID1, 5, "carlson")

        assertEquals(1, hangmanService.getGames(true).size)
        assertEquals("*******", game.getGuessingWordTracker().concatToString())
        assertEquals(5, game.getNumberOfRemainingGuesses())
    }

    @Test
    fun createTwoNewGames_verifyPlayerRemovedFromFirstGame() {
        val sessionID1 = "123"

        val game1 = hangmanService.createNewGame(sessionID1, null, null)
        val game2 = hangmanService.createNewGame(sessionID1, null, null)

        assertEquals(2, hangmanService.getGames(true).size)
        assertTrue(game1.getPlayers().isEmpty())
        assertEquals(1, game2.getPlayers().size)
    }

    @Test
    fun getGame_expectNotFoundException() {
        val sessionID1 = "123"

        assertThrows(GameNotFoundException::class.java) {
            hangmanService.getGame("123", null)
        }

    }

    @Test
    fun getGame_notANewPlayer() {
        val sessionID1 = "123"

        val createdGame = hangmanService.createNewGame(sessionID1, null, null)

        val game = hangmanService.getGame(createdGame.getId(), null)
        assertNotNull(game)
        assertEquals(createdGame, game)
        assertEquals(1, game.getPlayers().size)
    }

    @Test
    fun getGame_addANewPlayer() {
        val sessionID1 = "123"
        val sessionID2 = "456"

        val createdGame = hangmanService.createNewGame(sessionID1, null, null)

        val game = hangmanService.getGame(createdGame.getId(), sessionID2)

        assertNotNull(game)
        assertEquals(createdGame, game)
        assertEquals(2, game.getPlayers().size)
    }

    @Test
    fun getGames() {
        val sessionID1 = "123"

        val createdGame = hangmanService.createNewGame(sessionID1, 1, "hi")

        assertEquals(1, hangmanService.getGames(true).size)
        assertEquals(1, hangmanService.getGames(false).size)

        val game = hangmanService.guess('z', createdGame.getId(), sessionID1)

        assertTrue(hangmanService.getGames(true).isEmpty())
        assertEquals(1, hangmanService.getGames(false).size)
    }

    // TODO: Since im storing the games in an array it's hard to mock the Game class right now but when we change to storage
    // we can mock the Game objects.
    @Test
    fun guess_singlePlayer_performSingleWrongGuess_verifyGameOverException() {
        val sessionID1 = "123"

        val createdGame = hangmanService.createNewGame(sessionID1, 1, "hi")

        val game = hangmanService.guess('z', createdGame.getId(), sessionID1)

        assertThrows(GameAlreadyOverException::class.java) {
            hangmanService.guess('a', createdGame.getId(), sessionID1)
        }
    }

    @Test
    fun guess_singlePlayer_performDuplicateWrongGuess_verifyDuplicateWrongGuessException() {
        val sessionID1 = "123"

        val createdGame = hangmanService.createNewGame(sessionID1, 2, "hi")

        val game = hangmanService.guess('z', createdGame.getId(), sessionID1)

        assertThrows(DuplicateWrongGuessException::class.java) {
            hangmanService.guess('z', createdGame.getId(), sessionID1)
        }
    }

    @Test
    fun guess_singlePlayer_performCorrectGuess_verifyWordTracker() {
        val sessionID1 = "123"

        val createdGame = hangmanService.createNewGame(sessionID1, 2, "hi")

        val game = hangmanService.guess('h', createdGame.getId(), sessionID1)

        assertEquals("h*", game.getGuessingWordTracker().concatToString())
    }

    @Test
    fun guess_twoPlayers_performAlternateMoves_verifyNotPlayerTurnException() {
        val sessionID1 = "123"
        val sessionID2 = "456"

        val createdGame = hangmanService.createNewGame(sessionID1, 2, "hi")

        hangmanService.getGame(createdGame.getId(), sessionID2)

        var game = hangmanService.guess('h', createdGame.getId(), sessionID1)

        assertThrows(NotPlayerTurnException::class.java) {
            hangmanService.guess('z', createdGame.getId(), sessionID1)
        }

        assertEquals(sessionID2, game.getNextPlayer())

        game = hangmanService.guess('x', createdGame.getId(), sessionID2)

        assertEquals(sessionID1, game.getNextPlayer())
    }
}