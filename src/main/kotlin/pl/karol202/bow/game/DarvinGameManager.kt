package pl.karol202.bow.game

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pl.karol202.bow.bot.DarvinBot
import pl.karol202.bow.bot.agent.DQNAgent
import pl.karol202.bow.bot.environment.StandardEnvironment
import pl.karol202.bow.bot.neural.DarvinReinforcementNetwork
import pl.karol202.bow.model.GameState
import pl.karol202.bow.model.Order
import pl.karol202.bow.model.Player

class DarvinGameManager(private val coroutineScope: CoroutineScope,
                        private var data: DarvinReinforcementNetwork.Data?) : GameManager
{
	companion object
	{
		private const val GAME_RESULT_CHECK_INTERVAL_MILLIS = 100L

		private const val LEARN_RATE = 0.02f
		private const val DISCOUNT_FACTOR = 0.98f
	}

	private var game: Game? = null
	private val bots = mutableMapOf<Player.Side, DarvinBot>()

	override fun updateStateAndGetOrder(gameStateData: GameState.GameStateData): Order
	{
		val game = game ?: startNewGame(gameStateData)
		return game.update(gameStateData)
	}

	private fun startNewGame(gameStateData: GameState.GameStateData) = Game.create(gameStateData).also {
		game = it
		listenForEndOfGame(it)
	}

	private fun listenForEndOfGame(game: Game) = coroutineScope.launch {
		while(this@DarvinGameManager.game == game)
		{
			game.checkWinner()?.let { result ->
				stopGame(result)
				return@launch // Break
			}
			delay(GAME_RESULT_CHECK_INTERVAL_MILLIS)
		}
	}

	private fun Game.update(gameStateData: GameState.GameStateData): Order
	{
		updateState(gameStateData)
		val activePlayerSide = state.activePlayer.side
		val bot = bots.getOrPut(activePlayerSide) { createNewBot(activePlayerSide) }
		return bot.play(this, activePlayerSide)
	}

	private fun createNewBot(side: Player.Side) = DarvinBot(DQNAgent(data, LEARN_RATE, DISCOUNT_FACTOR), StandardEnvironment(side))

	private fun stopGame(winner: Player.Side)
	{
		game?.let { notifyBotsAboutEndOfGame(it, winner) }
		game = null
		bots.clear()
	}

	private fun notifyBotsAboutEndOfGame(game: Game, winner: Player.Side)
	{
		bots.forEach { _, bot -> bot.endGame(game, winner) }
	}
}