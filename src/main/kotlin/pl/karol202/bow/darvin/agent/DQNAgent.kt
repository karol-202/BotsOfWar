package pl.karol202.bow.darvin.agent

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import pl.karol202.bow.darvin.Action
import pl.karol202.bow.darvin.neural.DQNetwork
import pl.karol202.bow.game.Game
import pl.karol202.bow.model.GameState
import pl.karol202.bow.model.Player
import kotlin.random.Random

//Deep Q-network agent
class DQNAgent<ND : DQNetwork.Data>(private val playerSide: Player.Side,
                                    private val actionThreshold: Float,
                                    private val epsilon: Float,
                                    private val network: DQNetwork<ND>) : Agent<DQNAgent.Data<ND>>
{
	data class Data<ND : DQNetwork.Data>(val networkData: ND) : Agent.Data

	data class LearningSample(val evaluation: DQNetwork.Evaluation,
	                          val allErrors: List<FloatArray>)

	private data class Timestamp(val evaluations: List<DQNetwork.Evaluation>,
	                             val reward: Float)

	override val data get() = Data(network.data)

	private val logger: Logger = LoggerFactory.getLogger(javaClass)

	private var timestamps = mutableListOf<Timestamp>()
	private var currentEvaluations = mutableListOf<DQNetwork.Evaluation>()
	private var currentReward = 0f

	override fun <A : Action> pickAction(game: Game, state: GameState, actions: List<A>): A?
	{
		val evaluatedActions = evaluateActions(game, state, actions)
		val pickedAction =
				if(shouldPickRandomAction()) evaluatedActions.takeIf { it.isNotEmpty() }?.random()
				else evaluatedActions.maxBy { it.second.finalOutput }?.takeIf { it.second.finalOutput > actionThreshold }
		if(pickedAction != null)
			logger.debug("$playerSide picked ${pickedAction.first.javaClass.simpleName}: ${pickedAction.second.finalOutput}")
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

	fun calculateLearningSamples(discountFactor: Float): List<LearningSample>
	{
		var currentReward = 0f
		return timestamps.reversed().flatMap { (evaluations, reward) ->
			currentReward *= discountFactor
			currentReward += reward
			logger.debug("Reward: $currentReward")
			evaluations.map { evaluation ->
				val errors = network.calculateErrors(reward = currentReward, output = evaluation.finalOutput)
				LearningSample(evaluation, errors)
			}
		}.reversed()
	}
}