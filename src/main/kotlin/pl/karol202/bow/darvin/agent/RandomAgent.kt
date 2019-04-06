package pl.karol202.bow.darvin.agent

import pl.karol202.bow.darvin.Action
import pl.karol202.bow.game.Game
import pl.karol202.bow.model.GameState
import kotlin.random.Random

class RandomAgent(seed: Int) : Agent
{
	private val random = Random(seed)

	override fun <A : Action> pickAction(game: Game, state: GameState, actions: List<A>) = actions.random(random)

	override fun receiveReward(reward: Float) { }

	override fun moveToNextTimestamp() { }

	override fun teachAndReset() { }
}