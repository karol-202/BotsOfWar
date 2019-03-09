package pl.karol202.bow.service

import org.springframework.stereotype.Service
import pl.karol202.bow.bot.darvin.DarvinBot
import pl.karol202.bow.game.StandardGameManager
import pl.karol202.bow.model.GameState

@Service
class GameService
{
	private val gameManager = StandardGameManager(DarvinBot())

	fun updateStateAndGetOrder(gameState: GameState.GameStateData) = gameManager.updateStateAndGetOrder(gameState)
}