package pl.karol202.bow.game

import pl.karol202.bow.model.GameState
import pl.karol202.bow.model.Order

interface GameManager
{
	fun updateStateAndGetOrder(gameStateData: GameState.GameStateData): Order
}
