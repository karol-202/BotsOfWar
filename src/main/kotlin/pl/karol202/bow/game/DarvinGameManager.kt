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

interface DarvinGameListener
{
	fun onGameStart()

	fun onUpdate(turn: Int)

	//Indicates stop without teaching in case of not working of result getting coroutine
	fun onReset()

	fun onStopAndTeach(data: DarvinReinforcementNetwork.Data?)
}

class DarvinGameManager(private val coroutineScope: CoroutineScope,
                        private var data: DarvinReinforcementNetwork.Data?) : GameManager
{
	companion object
	{
		private const val GAME_RESULT_CHECK_INTERVAL_MILLIS = 100L

		private const val LEARN_RATE = 0.002f
		private const val DISCOUNT_FACTOR = 0.98f
	}

	private var gameListener: DarvinGameListener? = null

	private var game: Game? = null
	private val bots = mutableMapOf<Player.Side, DarvinBot<DQNAgent>>()

	override fun updateStateAndGetOrder(gameStateData: GameState.GameStateData): Order
	{
		val game = getOrEndGame(gameStateData.gameId) ?: startNewGame(gameStateData)
		return game.update(gameStateData)
	}

	private fun getOrEndGame(id: Int): Game?
	{
		val currentGame = game ?: return null
		return if(currentGame.id == id) game
		else
		{
			stopGame()
			gameListener?.onReset()
			null
		}
	}

	private fun startNewGame(gameStateData: GameState.GameStateData) = Game.create(gameStateData).also {
		game = it
		listenForEndOfGame(it)
		gameListener?.onGameStart()
	}

	private fun listenForEndOfGame(game: Game) = coroutineScope.launch {
		while(this@DarvinGameManager.game == game)
		{
			game.checkWinner()?.let { result ->
				stopGameAndTeach(result)
				return@launch // Break
			}
			delay(GAME_RESULT_CHECK_INTERVAL_MILLIS)
		}
	}

	private fun Game.update(gameStateData: GameState.GameStateData): Order
	{
		updateState(gameStateData)
		gameListener?.onUpdate(gameStateData.currentTurn)

		val activePlayerSide = state.activePlayer.side
		val bot = bots.getOrPut(activePlayerSide) { createNewBot(activePlayerSide) }
		return bot.play(this, activePlayerSide)
	}

	private fun createNewBot(side: Player.Side) =
			DarvinBot(DQNAgent(side, LEARN_RATE, DISCOUNT_FACTOR, data), StandardEnvironment(side))

	private fun stopGameAndTeach(winner: Player.Side)
	{
		game?.let { notifyBotsAboutEndOfGame(it, winner) }
		saveWinningNetworkData(winner)
		gameListener?.onStopAndTeach(data)
		stopGame()
	}

	private fun notifyBotsAboutEndOfGame(game: Game, winner: Player.Side)
	{
		bots.forEach { _, bot -> bot.endGame(game, winner) }
	}

	private fun saveWinningNetworkData(winner: Player.Side)
	{
		data = bots.entries.sortedBy { it.key == winner }.last().value.agent.getData()
	}

	private fun stopGame()
	{
		game = null
		bots.clear()
	}

	fun setGameListener(gameListener: DarvinGameListener)
	{
		this.gameListener = gameListener
	}
}