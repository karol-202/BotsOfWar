package pl.karol202.bow.game

import pl.karol202.bow.bot.Bot
import pl.karol202.bow.model.Action
import pl.karol202.bow.model.GameState
import pl.karol202.bow.model.Player

abstract class GameManager
{
	private var game: Game? = null
	private val bots = mutableMapOf<Player.Side, Bot>()

	fun updateGameStateAndGetOrders(gameStateData: GameState.GameStateData): List<Action>
	{
		val game = game?.also { it.updateGameState(gameStateData) } ?: startNewGame(gameStateData)
		val side = game.activePlayer
		val bot = bots.getOrPut(side) { createBot() }
		return bot.play(game, side)
	}

	private fun startNewGame(gameStateData: GameState.GameStateData): Game
	{
		val game = Game.create(gameStateData).also { this.game = it }
		bots.clear()
		return game
	}

	protected abstract fun createBot(): Bot
}