package pl.karol202.bow.darvin.neural

import com.google.gson.annotations.SerializedName
import pl.karol202.bow.darvin.Action
import pl.karol202.bow.game.Game
import pl.karol202.bow.model.GameState
import pl.karol202.bow.model.Player

interface DQNetwork<D : DQNetwork.Data>
{
	interface Data

	class Evaluation(@SerializedName("i") val input: FloatArray,
	                 @SerializedName("a") val allOutputs: List<FloatArray>, // Including final output
	                 @SerializedName("f") val finalOutput: Float)

	val data: D

	fun evaluateAndGetAllData(game: Game, state: GameState, action: Action, side: Player.Side): Evaluation

	fun calculateErrors(reward: Float, output: Float): List<FloatArray>

	fun learn(evaluation: Evaluation, allErrors: List<FloatArray>, learnRate: Float)
}