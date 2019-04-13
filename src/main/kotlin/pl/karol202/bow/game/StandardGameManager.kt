package pl.karol202.bow.game

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pl.karol202.bow.darvin.bot.Bot
import pl.karol202.bow.model.GameState
import pl.karol202.bow.model.Order
import pl.karol202.bow.model.Player

interface DarvinGameListener<B : Bot<*>>
{
	fun onGameStart()

	fun onUpdate(turn: Int)

	//Indicates stop without teaching
	fun onReset()

	fun onGameEnd(bots: List<B>)
}

class StandardGameManager<B : Bot<BD>, BD : Bot.Data>(private val coroutineScope: CoroutineScope,
                                                      private val botCreator: (Player.Side) -> B) : GameManager
{
	companion object
	{
		private const val GAME_RESULT_CHECK_INTERVAL_MILLIS = 100L
	}

	private var gameListener: DarvinGameListener<B>? = null

	private var game: Game? = null
	private val bots = mutableMapOf<Player.Side, B>()

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
		while(this@StandardGameManager.game == game)
		{
			game.checkWinner()?.let { result ->
				stopGameWithWinner(result)
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
		val bot = bots.getOrPut(activePlayerSide) { botCreator(activePlayerSide) }
		return bot.play(this, activePlayerSide)
	}

	private fun stopGameWithWinner(winner: Player.Side)
	{
		notifyBotsAboutEndOfGame(winner)
		stopGame()
	}

	private fun notifyBotsAboutEndOfGame(winner: Player.Side) = game?.let { game ->
		bots.forEach { (_, bot) -> bot.endGame(game, winner) }
	}

	private fun stopGame()
	{
		gameListener?.onGameEnd(bots.values.toList())
		game = null
		bots.clear()
	}

	fun setGameListener(gameListener: DarvinGameListener<B>)
	{
		this.gameListener = gameListener
	}
}