package pl.karol202.bow.bot.neural

import pl.karol202.axon.layer.LayerData
import pl.karol202.axon.layer.ReinforcementLayer
import pl.karol202.axon.layer.reinforcementNeuron
import pl.karol202.axon.network.NetworkData
import pl.karol202.axon.network.ReinforcementNetwork
import pl.karol202.axon.network.reinforcementLayer
import pl.karol202.axon.network.reinforcementNetwork
import pl.karol202.axon.neuron.NeuronData
import pl.karol202.axon.neuron.ReinforcementNeuron
import pl.karol202.axon.neuron.SigmoidalActivation
import pl.karol202.axon.specification.NetworkSpecification
import pl.karol202.axon.specification.createNetworkRandomly
import pl.karol202.bow.bot.Action
import pl.karol202.bow.bot.agent.DQNAgent
import pl.karol202.bow.game.Game
import pl.karol202.bow.model.Entity
import pl.karol202.bow.model.GameState
import pl.karol202.bow.model.LocalPosition
import pl.karol202.bow.util.FloatRange
import pl.karol202.bow.util.size

private typealias ReinforcementNetworkSpecification = NetworkSpecification<ReinforcementNetwork, ReinforcementLayer, ReinforcementNeuron>

class DarvinReinforcementNetwork(data: Data?)
{
	companion object
	{
		private val RANDOM_RANGE = -0.1f..0.1f
	}

	data class Data(val layers: List<List<FloatArray>>)

	private object Inputs
	{
		val size get() = root.size
		private val root = compoundInput {
			settings()
			map()
		}

		private fun InputContext.settings()
		{
			scalarInput { game, _, _ -> game.settings.miningPerTurn inLinearRange 0f..100f }
			Entity.Type.values().forEach { type ->
				scalarInput { game, _, _ -> game.settings.entitySettings.getValue(type).hp inLinearRange 0f..200f }
				scalarInput { game, _, _ -> game.settings.entitySettings.getValue(type).actionPoints inLinearRange 0f..10f }
				scalarInput { game, _, _ -> game.settings.entitySettings.getValue(type).rangeOfAttack inLinearRange 1f..5f }
				scalarInput { game, _, _ -> game.settings.entitySettings.getValue(type).damage inLinearRange 0f..50f }
				scalarInput { game, _, _ -> game.settings.entitySettings.getValue(type).cost inLinearRange 0f..500f }
			}
		}

		private fun InputContext.map()
		{
			repeat(14) { y ->
				repeat(19) { x ->
					val position = LocalPosition(x, y)
					scalarInput { game, _, _ -> if(game.isPositionWalkable(position)) 1f else 0f }
				}
			}

			//positionInput { _, state, _ -> state.player1 }
		}

		private infix fun Int.inLinearRange(range: FloatRange) = this.toFloat().inLinearRange(range)

		private infix fun Float.inLinearRange(range: FloatRange) = (this - range.start) / range.size

		fun transformToInputArray(game: Game, state: GameState, action: Action) = FloatArray(size).also {
			root.write(it, 0, game, state, action)
		}
	}

	private val network = reinforcementNetwork(Inputs.size) {
		reinforcementLayer {
			repeat(500) { reinforcementNeuron(SigmoidalActivation(1f)) }
		}
		reinforcementLayer {
			repeat(50) { reinforcementNeuron(SigmoidalActivation(1f)) }
		}
		reinforcementLayer {
			reinforcementNeuron(SigmoidalActivation(1f))
		}
	}.createNetworkWithData(data)

	private fun ReinforcementNetworkSpecification.createNetworkWithData(data: Data?) =
			if(data == null) createNetworkRandomly(RANDOM_RANGE)
			else createNetwork(createNetworkData(data))

	private fun createNetworkData(data: Data) =
			NetworkData.fromList(data.layers.map { layer ->
				LayerData.fromList(layer.map { neuron ->
					NeuronData.fromArray(neuron)
				})
			})

	fun evaluateAndGetAllData(game: Game, state: GameState, action: Action): DQNAgent.Evaluation
	{
		val input = Inputs.transformToInputArray(game, state, action)
		val outputs = network.calculateAndGetIntermediateOutputs(input)
		val finalOutput = outputs.last().single() // Network has 1 output
		return DQNAgent.Evaluation(input, outputs, finalOutput)
	}

	fun calculateErrors(reward: Float) =
			network.backpropagateErrorAndGetIntermediateErrors(floatArrayOf(reward)) // Network has 1 output

	fun learn(evaluation: DQNAgent.Evaluation, allErrors: List<FloatArray>, learnRate: Float)
	{
		network.learn(evaluation.input, evaluation.allOutputs, allErrors, learnRate)
	}
}