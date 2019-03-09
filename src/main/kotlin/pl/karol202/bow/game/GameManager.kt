package pl.karol202.bow.game

import pl.karol202.bow.bot.Bot
import pl.karol202.bow.model.GameState
import pl.karol202.bow.model.Order
import pl.karol202.bow.model.Player

abstract class GameManager
{
	private var game: Game? = null
	private val bots = mutableMapOf<Player.Side, Bot>()

	fun updateStateAndGetOrder(gameStateData: GameState.GameStateData): Order
	{
		val game = game?.also { it.updateGameState(gameStateData) } ?: startNewGame(gameStateData)
		val player = game.activePlayer
		val bot = bots.getOrPut(player.side) { createBot() }
		return bot.play(game, player.side)
	}

	private fun startNewGame(gameStateData: GameState.GameStateData): Game
	{
		val game = Game.create(gameStateData).also { this.game = it }
		bots.clear()
		return game
	}

	protected abstract fun createBot(): Bot
}