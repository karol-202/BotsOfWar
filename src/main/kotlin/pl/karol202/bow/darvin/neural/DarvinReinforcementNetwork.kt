package pl.karol202.bow.darvin.neural

import pl.karol202.axon.layer.LayerData
import pl.karol202.axon.layer.ReinforcementLayer
import pl.karol202.axon.layer.reinforcementNeuron
import pl.karol202.axon.network.NetworkData
import pl.karol202.axon.network.ReinforcementNetwork
import pl.karol202.axon.network.reinforcementLayer
import pl.karol202.axon.network.reinforcementNetwork
import pl.karol202.axon.neuron.NeuronData
import pl.karol202.axon.neuron.ReinforcementNeuron
import pl.karol202.axon.neuron.TangensoidalActivation
import pl.karol202.axon.specification.NetworkSpecification
import pl.karol202.axon.specification.createNetworkRandomly
import pl.karol202.bow.darvin.*
import pl.karol202.bow.darvin.agent.DQNAgent
import pl.karol202.bow.game.Game
import pl.karol202.bow.model.*
import pl.karol202.bow.util.FloatRange
import pl.karol202.bow.util.size

private typealias ReinforcementNetworkSpecification = NetworkSpecification<ReinforcementNetwork, ReinforcementLayer, ReinforcementNeuron>

class DarvinReinforcementNetwork(initialData: Data?)
{
	companion object
	{
		private val RANDOM_RANGE = -0.1f..0.1f
	}

	data class Data(val layers: List<List<FloatArray>>)

	private object Inputs
	{
		private const val MAP_WIDTH = 19
		private const val MAP_HEIGHT = 14
		private const val MINES = 4
		private const val ENTITIES_MAX = 20

		val size get() = root.size
		private val root = compoundInput {
			settings()
			map()
			players()
			mines()
			bases()
			entities()
			action()
		}

		private fun InputContext.settings()
		{
			scalarInput { game, _, _, _ -> game.settings.miningPerTurn inLinearRange 0f..100f }
			Entity.Type.values().forEach { type ->
				fun setting(settingSelector: Entity.() -> Float) =
						scalarInput { game, _, _, _ -> game.settings.entitySettings.getValue(type).settingSelector() }

				setting { hp inLinearRange 0f..200f }
				setting { actionPoints inLinearRange 0f..10f }
				setting { rangeOfAttack inLinearRange 0f..5f }
				setting { damage inLinearRange 0f..50f }
				setting { cost inLinearRange 0f..500f }
			}
		}

		private fun InputContext.map()
		{
			repeat(MAP_HEIGHT) { y ->
				repeat(MAP_WIDTH) { x ->
					val position = LocalPosition(x, y)
					scalarInput { game, _, _, _ -> if(game.isPositionWalkable(position)) 1f else -1f }
				}
			}
		}
		
		private fun InputContext.mines()
		{
			repeat(MINES) { mineIndex ->
				positionInput { _, state, _, _ -> state.mines[mineIndex].position }
				scalarInput { _, state, _, _ -> state.mines[mineIndex].goldLeft inLinearRange 0f..3000f }
				scalarInput { _, state, _, _ -> state.mines[mineIndex].miningPerWorker inLinearRange 0f..50f }
				scalarInput { _, state, _, _ -> state.mines[mineIndex].workersNumber inLinearRange 0f..10f }
				scalarInput { _, state, _, side -> when(state.mines[mineIndex].owner.correspondingSide) {
					side -> 1f
					side.opposite -> -1f
					else -> 0f
				} }
			}
		}

		private fun InputContext.players()
		{
			fun player(sideSelector: Player.Side.() -> Player.Side) =
					scalarInput { _, state, _, side -> state.getPlayer(side.sideSelector()).gold inLinearRange 0f..2000f }

			player { this }
			player { opposite }
		}
		
		private fun InputContext.bases()
		{
			fun base(sideSelector: Player.Side.() -> Player.Side)
			{
				positionInput { _, state, _, side -> state.getPlayer(side.sideSelector()).base.position }
				scalarInput { _, state, _, side -> state.getPlayer(side.sideSelector()).base.hp inLinearRange 0f..1000f }
			}

			base { this }
			base { opposite }
		}

		private fun InputContext.entities()
		{
			fun entity(entitySelector: (GameState, Player.Side) -> Entity?)
			{
				oneOfNInput(Entity.Type.values().size + 1) { _, state, _, side ->
					entitySelector(state, side)?.type?.ordinal?.plus(1) ?: 0
				}
				positionInput { _, state, _, side -> entitySelector(state, side)?.position ?: LocalPosition.zero }
				scalarInput { _, state, _, side -> (entitySelector(state, side)?.hp ?: 0) inLinearRange 0f..200f }
				scalarInput { _, state, _, side -> (entitySelector(state, side)?.actionPoints ?: 0) inLinearRange 0f..10f }
				scalarInput { _, state, _, side -> (entitySelector(state, side)?.rangeOfAttack ?: 0) inLinearRange 0f..5f }
				scalarInput { _, state, _, side -> (entitySelector(state, side)?.damage ?: 0) inLinearRange 0f..50f }
				scalarInput { _, state, _, side -> (entitySelector(state, side)?.cost ?: 0) inLinearRange 0f..500f }
			}

			repeat(ENTITIES_MAX) { entityIndex ->
				entity { state, side -> state.getPlayer(side).entities.getOrNull(entityIndex) }
				entity { state, side -> state.getPlayer(side.opposite).entities.getOrNull(entityIndex) }
			}
		}

		private fun InputContext.action()
		{
			oneOfNInput(ActionModel.Type.values().size) { _, _, action, _ -> action.toModelAction().type.ordinal }

			actionAttack()
			actionMove()
			actionEntrenchment()
			actionRecruitment()
		}

		private fun InputContext.actionAttack()
		{
			oneOfNInput(ENTITIES_MAX + 1) { _, state, action, side ->
				(action as? Attack)?.let { attack -> state.getPlayer(side).entities.indexOf(attack.attacker) + 1 } ?: 0
			}
			oneOfNInput(ENTITIES_MAX + 2) { _, state, action, side ->
				(action as? Attack)?.let { attack ->
					when(attack)
					{
						is EntityAttack -> state.getPlayer(side).entities.indexOf(attack.victim)
						is BaseAttack -> 1
					}
				} ?: 0
			}
		}

		private fun InputContext.actionMove()
		{
			oneOfNInput(ENTITIES_MAX + 1) { _, state, action, side ->
				(action as? Move)?.let { move -> state.getPlayer(side).entities.indexOf(move.entity) + 1 } ?: 0
			}
			oneOfNInput(Direction.values().size + 1) { _, _, action, _ ->
				(action as? Move)?.let { move -> move.direction.ordinal + 1 } ?: 0
			}
		}

		private fun InputContext.actionEntrenchment()
		{
			oneOfNInput(ENTITIES_MAX + 1) { _, state, action, side ->
				(action as? Entrenchment)?.let { entrenchment -> state.getPlayer(side).entities.indexOf(entrenchment.entity) + 1 } ?: 0
			}
		}

		private fun InputContext.actionRecruitment()
		{
			oneOfNInput(Entity.Type.values().size + 1) { _, _, action, _ ->
				(action as? Recruitment)?.entitySettings?.type?.ordinal?.plus(1) ?: 0
			}
		}

		private infix fun Int.inLinearRange(range: FloatRange) = this.toFloat().inLinearRange(range)

		private infix fun Float.inLinearRange(range: FloatRange) = (this - range.start) / range.size

		fun transformToInputArray(game: Game, state: GameState, action: Action, side: Player.Side) = FloatArray(size).also {
			root.write(it, 0, game, state, action, side)
		}
	}

	private val network = reinforcementNetwork(Inputs.size) {
		reinforcementLayer {
			repeat(500) { reinforcementNeuron(TangensoidalActivation(1f)) }
		}
		reinforcementLayer {
			repeat(50) { reinforcementNeuron(TangensoidalActivation(1f)) }
		}
		reinforcementLayer {
			reinforcementNeuron(TangensoidalActivation(1f))
		}
	}.createNetworkWithData(initialData)

	private fun ReinforcementNetworkSpecification.createNetworkWithData(data: Data?) =
			if(data == null) createNetworkRandomly(RANDOM_RANGE)
			else createNetwork(createNetworkData(data))

	private fun createNetworkData(data: Data) =
			NetworkData.fromList(data.layers.map { layer ->
				LayerData.fromList(layer.map { neuron ->
					NeuronData.fromArray(neuron)
				})
			})

	fun evaluateAndGetAllData(game: Game, state: GameState, action: Action, side: Player.Side): DQNAgent.Evaluation
	{
		val input = Inputs.transformToInputArray(game, state, action, side)
		val outputs = network.calculateAndGetIntermediateOutputs(input)
		val finalOutput = outputs.last().single() // Network has 1 output
		return DQNAgent.Evaluation(input, outputs, finalOutput)
	}

	fun calculateErrors(reward: Float, output: Float) =
			network.backpropagateErrorAndGetIntermediateErrors(floatArrayOf(reward * output)) // Network has 1 output

	fun learn(evaluation: DQNAgent.Evaluation, allErrors: List<FloatArray>, learnRate: Float)
	{
		network.learn(evaluation.input, evaluation.allOutputs, allErrors, learnRate)
	}

	fun getData() = Data(network.networkData.getLayersData().map { layer ->
		layer.getNeuronsData().map { neuron ->
			neuron.getWeights().toFloatArray()
		}
	})
}