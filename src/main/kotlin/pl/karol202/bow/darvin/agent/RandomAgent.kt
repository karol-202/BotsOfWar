package pl.karol202.bow.darvin.agent

import pl.karol202.bow.darvin.Action
import pl.karol202.bow.game.Game
import pl.karol202.bow.model.GameState
import kotlin.random.Random

class RandomAgent(seed: Int) : Agent
{
	private val random = Random(seed)

	override fun evaluateAction(game: Game, state: GameState, action: Action) = (random.nextFloat() * 2) - 1

	override fun receiveReward(reward: Float) { }

	override fun moveToNextTimestamp() { }

	override fun teachAndReset() { }
}