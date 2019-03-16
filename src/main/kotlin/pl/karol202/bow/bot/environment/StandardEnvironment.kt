package pl.karol202.bow.bot.environment

import pl.karol202.bow.model.GameState
import pl.karol202.bow.model.Player

class StandardEnvironment : Environment
{
	override fun updateStateAndGetRewards(newState: GameState, winner: Player.Side?): Float
	{
		return 0f
	}
}