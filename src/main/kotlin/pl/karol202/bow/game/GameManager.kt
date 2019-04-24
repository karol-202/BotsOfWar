package pl.karol202.bow.game

import pl.karol202.bow.model.GameState
import pl.karol202.bow.model.Order

interface GameManager
{
	suspend fun updateStateAndGetOrder(gameStateData: GameState.GameStateData): Order
}
