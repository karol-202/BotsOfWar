package pl.karol202.bow.game

import pl.karol202.bow.bot.Bot
import pl.karol202.bow.model.GameState
import pl.karol202.bow.model.Order

class StandardGameManager(private val bot: Bot) : GameManager
{
	private var game: Game? = null

	override fun updateStateAndGetOrder(gameStateData: GameState.GameStateData): Order
	{
		val game = game?.also { it.updateState(gameStateData) } ?: startNewGame(gameStateData)
		//Check if game is ended
		val player = game.state.activePlayer
		return bot.play(game, player.side)
	}
	
	private fun startNewGame(gameStateData: GameState.GameStateData) =
			Game.create(gameStateData).also { this.game = it }

	private fun stopGame()
	{
		game = null
	}
}