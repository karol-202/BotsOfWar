package pl.karol202.bow.darvin.environment

import pl.karol202.bow.model.GameState
import pl.karol202.bow.model.Player

interface Environment
{
	fun updateStateAndGetReward(newState: GameState, winner: Player.Side?): Float
}