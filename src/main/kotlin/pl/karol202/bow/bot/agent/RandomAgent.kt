package pl.karol202.bow.bot.agent

import pl.karol202.bow.bot.Action
import pl.karol202.bow.model.GameState
import kotlin.random.Random

class RandomAgent(seed: Int) : Agent
{
	private val random = Random(seed)

	override fun evaluateAction(state: GameState, action: Action) = (random.nextFloat() * 2) - 1

	override fun receiveReward(reward: Float) { }

	override fun moveToNextTimestamp() { }

	override fun teachAllAndReset() { }
}