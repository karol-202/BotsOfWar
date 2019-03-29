package pl.karol202.bow.darvin.agent

import pl.karol202.bow.darvin.Action
import pl.karol202.bow.darvin.neural.DarvinReinforcementNetwork
import pl.karol202.bow.game.Game
import pl.karol202.bow.model.GameState
import pl.karol202.bow.model.Player

//Deep Q-network agent
class DQNAgent(private val playerSide: Player.Side,
               private val learnRate: Float,
               private val discountFactor: Float,
               data: DarvinReinforcementNetwork.Data?) : Agent
{
	class Evaluation(val input: FloatArray,
	                 val allOutputs: List<FloatArray>, // Including final output
	                 val finalOutput: Float)

	private data class Timestamp(val evaluations: List<Evaluation>,
	                             val reward: Float)

	private data class LearningSample(val evaluation: Evaluation,
	                                  val allErrors: List<FloatArray>)

	private val network = DarvinReinforcementNetwork(data)

	private var timestamps = mutableListOf<Timestamp>()
	private var currentEvaluations = mutableListOf<Evaluation>()
	private var currentReward = 0f

	override fun evaluateAction(game: Game, state: GameState, action: Action) =
			network.evaluateAndGetAllData(game, state, action, playerSide).also { currentEvaluations.add(it) }.finalOutput

	override fun receiveReward(reward: Float)
	{
		currentReward += reward
	}

	override fun moveToNextTimestamp()
	{
		timestamps.add(Timestamp(currentEvaluations, currentReward))
		currentEvaluations = mutableListOf()
		currentReward = 0f
	}

	//Assumes that moveToNextTimestamp() has been called
	override fun teachAndReset()
	{
		teachNetwork(calculateLearningData())
		timestamps = mutableListOf()
	}

	private fun calculateLearningData(): List<LearningSample>
	{
		var currentReward = 0f
		return timestamps.reversed().flatMap { (evaluations, reward) ->
			currentReward *= discountFactor
			currentReward += reward
			evaluations.map { evaluation ->
				val errors = network.calculateErrors(reward = currentReward, output = evaluation.finalOutput)
				LearningSample(evaluation, errors)
			}
		}
	}

	private fun teachNetwork(learningData: List<LearningSample>)
	{
		learningData.forEach { (evaluation, allErrors) -> network.learn(evaluation, allErrors, learnRate) }
	}

	fun getData() = network.getData()
}