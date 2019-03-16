package pl.karol202.bow.bot.environment

import pl.karol202.bow.model.GameState

interface Environment
{
	fun updateStateAndGetRewards(newState: GameState): Float
}