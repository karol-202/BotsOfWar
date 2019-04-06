package pl.karol202.bow.darvin.agent

import pl.karol202.bow.darvin.Action
import pl.karol202.bow.game.Game
import pl.karol202.bow.model.GameState

interface Agent
{
	fun <A : Action> pickAction(game: Game, state: GameState, actions: List<A>): A?

	fun receiveReward(reward: Float)

	fun moveToNextTimestamp()

	fun teachAndReset()
}