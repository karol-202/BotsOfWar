package pl.karol202.bow.service

import org.springframework.stereotype.Service
import pl.karol202.bow.game.DarvinGameManager
import pl.karol202.bow.model.GameState

@Service
class GameService
{
	private val gameManager = DarvinGameManager(null)

	fun updateStateAndGetOrder(gameState: GameState.GameStateData) = gameManager.updateStateAndGetOrder(gameState)
}