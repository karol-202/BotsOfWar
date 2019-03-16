package pl.karol202.bow.game

import pl.karol202.bow.bot.DarvinBot
import pl.karol202.bow.bot.agent.DQNAgent
import pl.karol202.bow.bot.environment.StandardEnvironment
import pl.karol202.bow.bot.neural.DarvinReinforcementNetwork
import pl.karol202.bow.model.GameState
import pl.karol202.bow.model.Order
import pl.karol202.bow.model.Player

class DarvinGameManager(private var data: DarvinReinforcementNetwork.Data?) : GameManager
{
	companion object
	{
		private const val LEARN_RATE = 0.02f
		private const val DISCOUNT_FACTOR = 0.98f
	}

	private var game: Game? = null
	private val bots = mutableMapOf<Player.Side, DarvinBot>()

	override fun updateStateAndGetOrder(gameStateData: GameState.GameStateData): Order
	{
		val game = game ?: Game.create(gameStateData).also { game = it }
		return game.update(gameStateData)
	}

	private fun Game.update(gameStateData: GameState.GameStateData): Order
	{
		updateGameState(gameStateData)
		// TODO Check if game is ended
		val activePlayerSide = activePlayer.side
		val bot = bots.getOrPut(activePlayerSide) { createNewBot() }
		return bot.play(this, activePlayerSide)
	}

	private fun createNewBot() = DarvinBot(DQNAgent(data, LEARN_RATE, DISCOUNT_FACTOR), StandardEnvironment())

	private fun stopGame()
	{
		game?.let { notifyBotsAboutEndOfGame(it) }
		game = null
		bots.clear()
	}

	private fun notifyBotsAboutEndOfGame(game: Game)
	{
		bots.forEach { _, bot -> bot.endGame(game) }
	}
}