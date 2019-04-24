package pl.karol202.bow.darvin.agent

import pl.karol202.bow.darvin.Action
import pl.karol202.bow.game.Game
import pl.karol202.bow.model.GameState
import kotlin.random.Random

class RandomAgent(private val seed: Int) : Agent<RandomAgent.Data>
{
	data class Data(val seed: Int) : Agent.Data

	private val random = Random(seed)

	override val data get() = Data(seed)

	constructor(data: Data) : this(data.seed)

	override suspend fun <A : Action> pickAction(game: Game, state: GameState, actions: List<A>) = actions.random(random)

	override fun receiveReward(reward: Float) { }

	override fun moveToNextTimestamp() { }
}