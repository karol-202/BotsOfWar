package pl.karol202.bow.darvin.agent

import com.google.gson.annotations.SerializedName
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import pl.karol202.bow.darvin.Action
import pl.karol202.bow.darvin.neural.DarvinReinforcementNetwork
import pl.karol202.bow.game.Game
import pl.karol202.bow.model.GameState
import pl.karol202.bow.model.Player
import kotlin.random.Random

//Deep Q-network agent
class DQNAgent(private val playerSide: Player.Side,
               private val actionThreshold: Float,
               private val learnRate: Float,
               private val discountFactor: Float,
               private val epsilon: Float,
               private val learningSamplesPerEpoch: Int,
               private val learningSamplesMemorySize: Int,
               initialData: Data?) : Agent
{
	data class Data(val networkData: DarvinReinforcementNetwork.Data,
	                val learningSamples: List<LearningSample>)

	data class LearningSample(val evaluation: Evaluation,
	                          val allErrors: List<FloatArray>)

	private data class Timestamp(val evaluations: List<Evaluation>,
	                             val reward: Float)

	class Evaluation(@SerializedName("i") val input: FloatArray,
	                 @SerializedName("a") val allOutputs: List<FloatArray>, // Including final output
	                 @SerializedName("f") val finalOutput: Float)

	private val logger: Logger = LoggerFactory.getLogger(javaClass)
	private val network = DarvinReinforcementNetwork(initialData?.networkData)
	private val learningSamples = initialData?.learningSamples?.toMutableList() ?: mutableListOf()

	private var timestamps = mutableListOf<Timestamp>()
	private var currentEvaluations = mutableListOf<Evaluation>()
	private var currentReward = 0f

	override fun <A : Action> pickAction(game: Game, state: GameState, actions: List<A>): A?
	{
		val evaluatedActions = evaluateActions(game, state, actions)
		val pickedAction =
				if(shouldPickRandomAction()) evaluatedActions.takeIf { it.isNotEmpty() }?.random()
				else evaluatedActions.maxBy { it.second.finalOutput }?.takeIf { it.second.finalOutput > actionThreshold }
		logger.debug("$playerSide picked ${pickedAction?.first?.javaClass?.simpleName}: ${pickedAction?.second?.finalOutput}")
		return pickedAction?.also { currentEvaluations.add(it.second) }?.first
	}

	private fun shouldPickRandomAction() = Random.nextFloat() < epsilon

	private fun <A : Action> evaluateActions(game: Game, state: GameState, actions: List<A>) =
			actions.map { action -> action to evaluateAction(game, state, action) }

	private fun evaluateAction(game: Game, state: GameState, action: Action) =
			network.evaluateAndGetAllData(game, state, action, playerSide)

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
		learningSamples += calculateLearningSamples()
		checkLearningSamplesMemorySize()
		teachNetwork()

		timestamps = mutableListOf()
	}

	private fun calculateLearningSamples(): List<LearningSample>
	{
		var currentReward = 0f
		return timestamps.reversed().flatMap { (evaluations, reward) ->
			currentReward *= discountFactor
			currentReward += reward
			evaluations.map { evaluation ->
				val errors = network.calculateErrors(reward = currentReward, output = evaluation.finalOutput)
				LearningSample(evaluation, errors)
			}
		}.reversed()
	}

	private fun checkLearningSamplesMemorySize()
	{
		//If memory size is not exceeded, lambda won't be called
		repeat(learningSamples.size - learningSamplesMemorySize) {
			learningSamples.removeAt(0)
		}
	}

	private fun teachNetwork()
	{
		learningSamples.shuffled().take(learningSamplesPerEpoch).forEach { sample ->
			teachNetworkOneSample(sample)
		}
	}

	private fun teachNetworkOneSample(learningSample: LearningSample)
	{
		network.learn(learningSample.evaluation, learningSample.allErrors, learnRate)
	}

	fun getData() = Data(network.getData(), learningSamples)
}