package pl.karol202.bow.bot.agent

import pl.karol202.bow.bot.Action
import pl.karol202.bow.model.GameState

interface Agent
{
	fun evaluateAction(state: GameState, action: Action): Float

	fun receiveReward(reward: Float)

	fun moveToNextTimestamp()

	fun teachAllAndReset()
}