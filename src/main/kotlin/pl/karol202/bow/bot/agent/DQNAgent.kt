package pl.karol202.bow.bot.agent

import pl.karol202.bow.bot.Action
import pl.karol202.bow.bot.neural.DarvinReinforcementNetwork
import pl.karol202.bow.game.Game
import pl.karol202.bow.model.GameState

//Deep Q-network agent
class DQNAgent(data: DarvinReinforcementNetwork.Data?,
               private val learnRate: Float,
               private val discountFactor: Float) : Agent
{
	class Evaluation(val input: FloatArray,
	                 val allOutputs: List<FloatArray>, // Including final output
	                 val finalOutput: Float)

	private data class LearningData(val evaluation: Evaluation,
	                                val allErrors: List<FloatArray>)

	private data class Timestamp(val evaluations: List<Evaluation>,
	                             val reward: Float)

	private val network = DarvinReinforcementNetwork(data)

	private var timestamps = mutableListOf<Timestamp>()
	private var currentEvaluations = mutableListOf<Evaluation>()
	private var currentReward = 0f

	override fun evaluateAction(game: Game, state: GameState, action: Action) =
			network.evaluateAndGetAllData(game, state, action).also { currentEvaluations.add(it) }.finalOutput

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

	override fun teachAllAndReset()
	{
		moveToNextTimestamp()
		teachNetwork(calculateLearningData())
		timestamps = mutableListOf()
	}

	private fun calculateLearningData(): List<LearningData>
	{
		var currentReward = 0f
		return timestamps.reversed().flatMap { (evaluations, reward) ->
			currentReward *= discountFactor
			currentReward += reward
			evaluations.map { evaluation ->
				val errors = network.calculateErrors(currentReward)
				LearningData(evaluation, errors)
			}
		}
	}

	private fun teachNetwork(learningData: List<LearningData>)
	{
		learningData.forEach { (evaluation, allErrors) -> network.learn(evaluation, allErrors, learnRate) }
	}
}