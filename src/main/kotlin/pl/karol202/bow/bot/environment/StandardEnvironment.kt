package pl.karol202.bow.bot.environment

import pl.karol202.bow.model.GameState

class StandardEnvironment : Environment
{
	override fun updateStateAndGetRewards(newState: GameState): Float
	{
		return 0f
	}
}