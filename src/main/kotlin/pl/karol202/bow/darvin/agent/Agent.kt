package pl.karol202.bow.darvin.agent

import pl.karol202.bow.darvin.Action
import pl.karol202.bow.game.Game
import pl.karol202.bow.model.GameState

interface Agent<D : Agent.Data>
{
	interface Data

	val data: D

	suspend fun <A : Action> pickAction(game: Game, state: GameState, actions: List<A>): A?

	fun receiveReward(reward: Float)

	fun moveToNextTimestamp()
}