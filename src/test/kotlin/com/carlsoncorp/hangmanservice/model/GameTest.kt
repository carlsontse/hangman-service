package com.carlsoncorp.hangmanservice.model

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class GameTest {

    val MAX_NUMBER_OF_GUESSES = 2
    val SECRET_WORD = "hahA"
    val SESSION_ID = "123"
    lateinit var game: Game

    @BeforeEach
    fun setUp() {
        game = Game(MAX_NUMBER_OF_GUESSES, SECRET_WORD, SESSION_ID)
    }

    @AfterEach
    fun tearDown() {
    }

    @Test
    fun testInit() {
        assertEquals(SECRET_WORD.length, game.getGuessingWordTracker().size)
        assertEquals(1, game.getPlayers().size)
        assertEquals(SESSION_ID, game.getNextPlayer())
        assertTrue(game.isPlayerTurn(SESSION_ID))
    }

    @Test
    fun updateGuessWordTracker_singlePlayer_foundGuessLetter_gameContinues() {
        game.updateGuessWordTracker('a', SESSION_ID)
        assertEquals("*a*A", game.getGuessingWordTracker().concatToString())
        assertFalse(game.isGameOver())
    }

    @Test
    fun updateGuessWordTracker_singlePlayer_foundGuessLetter_gameWon() {
        game.updateGuessWordTracker('a', SESSION_ID)
        game.updateGuessWordTracker('h', SESSION_ID)
        assertEquals("hahA", game.getGuessingWordTracker().concatToString())
        assertTrue(game.isGameOver())
        assertEquals(GameState.GAME_OVER_WIN, game.getState())
    }

    @Test
    fun updateGuessWordTracker_singlePlayer_wrongGuess_gameContinues() {
        game.updateGuessWordTracker('z', SESSION_ID)
        assertEquals("****", game.getGuessingWordTracker().concatToString())
        assertEquals(1, game.getWrongGuesses().size)
        assertEquals('z', game.getWrongGuesses()[0].guessLetter)
        assertEquals(SESSION_ID, game.getWrongGuesses()[0].sessionId)
        assertFalse(game.isGameOver())
    }

    @Test
    fun updateGuessWordTracker_singlePlayer_wrongGuess_gameEnds() {
        game.updateGuessWordTracker('z', SESSION_ID)
        game.updateGuessWordTracker('y', SESSION_ID)
        assertEquals("****", game.getGuessingWordTracker().concatToString())
        assertEquals(2, game.getWrongGuesses().size)
        assertEquals('z', game.getWrongGuesses()[0].guessLetter)
        assertEquals(SESSION_ID, game.getWrongGuesses()[0].sessionId)
        assertEquals('y', game.getWrongGuesses()[1].guessLetter)
        assertEquals(SESSION_ID, game.getWrongGuesses()[1].sessionId)
        assertTrue(game.isGameOver())
        assertEquals(GameState.GAME_OVER_LOSS, game.getState())
    }

    @Test
    fun updateGuessWordTracker_twoPlayers() {
        var player2 = "456"

        game.addPlayerIfNew(player2)
        assertEquals(2, game.getPlayers().size)

        game.updateGuessWordTracker('a', SESSION_ID)
        assertEquals("*a*A", game.getGuessingWordTracker().concatToString())
        assertEquals(0, game.getWrongGuesses().size)
        assertEquals(player2, game.getNextPlayer())

        game.updateGuessWordTracker('y', player2)
        assertEquals("*a*A", game.getGuessingWordTracker().concatToString())
        assertEquals(1, game.getWrongGuesses().size)
        assertEquals('y', game.getWrongGuesses()[0].guessLetter)
        assertEquals(player2, game.getWrongGuesses()[0].sessionId)
        assertEquals(SESSION_ID, game.getNextPlayer())

        assertFalse(game.isGameOver())
    }

    @Test
    fun removePlayerIfFound_notFound_noop() {
        game.removePlayerIfFound("xva12312")
        assertEquals(1, game.getPlayers().size)
    }

    @Test
    fun removePlayerIfFound_removeOnlyPlayer() {
        game.removePlayerIfFound(SESSION_ID)
        assertEquals(0, game.getPlayers().size)
    }

    @Test
    fun removePlayerIfFound_removeActivePlayer() {
        game.addPlayerIfNew("456")
        assertEquals(2, game.getPlayers().size)
        game.removePlayerIfFound(SESSION_ID)
        assertEquals(1, game.getPlayers().size)
        assertEquals("456", game.getNextPlayer())
    }

    @Test
    fun removePlayerIfFound_removeNonActivePlayer() {
        game.addPlayerIfNew("456")
        assertEquals(2, game.getPlayers().size)
        game.removePlayerIfFound("456")
        assertEquals(1, game.getPlayers().size)
        assertEquals(SESSION_ID, game.getNextPlayer())
    }
}