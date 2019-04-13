package pl.karol202.bow.darvin.neural

import pl.karol202.axon.layer.LayerData
import pl.karol202.axon.layer.reinforcementNeuron
import pl.karol202.axon.network.NetworkData
import pl.karol202.axon.network.ReinforcementNetwork
import pl.karol202.axon.network.reinforcementLayer
import pl.karol202.axon.network.reinforcementNetwork
import pl.karol202.axon.neuron.NeuronData
import pl.karol202.axon.neuron.TangensoidalActivation
import pl.karol202.axon.specification.createNetworkRandomly
import pl.karol202.bow.darvin.*
import pl.karol202.bow.game.Game
import pl.karol202.bow.model.*
import pl.karol202.bow.util.FloatRange
import pl.karol202.bow.util.size

class AxonDQNetwork private constructor(private val network: ReinforcementNetwork) : DQNetwork<AxonDQNetwork.Data>
{
	data class Data(val layers: List<List<FloatArray>>) : DQNetwork.Data

	private object Inputs
	{
		private const val MAP_AREA_RANGE = 7 // -------*-------

		val size get() = root.size
		private val root = compoundInput {
			players()
			relativeMap()

			action()
		}

		private fun InputContext.players()
		{
			fun player(sideSelector: Player.Side.() -> Player.Side)
			{
				scalarInput { _, state, _, side -> state.getPlayer(side.sideSelector()).gold inLinearRange 0f..2000f }
				scalarInput { _, state, _, side -> state.getPlayer(side.sideSelector()).base.hp inLinearRange 0f..1000f }
			}

			player { this }
			player { opposite }
		}

		private fun InputContext.relativeMap()
		{
			for(x in -MAP_AREA_RANGE..MAP_AREA_RANGE)
			{
				for(y in -MAP_AREA_RANGE..MAP_AREA_RANGE) relativeMapCell(LocalPosition(x, y))
			}
		}

		private fun InputContext.relativeMapCell(offset: LocalPosition)
		{
			fun globalPosition(state: GameState, action: Action, side: Player.Side) = when(action)
			{
				is Attack -> action.attacker.position
				is Move -> action.entity.position
				is Entrenchment -> action.entity.position
				is Recruitment -> state.getPlayer(side).base.position
			} + offset

			scalarInput { game, state, action, side -> // Is walkable (only map)
				if(game.isPositionWalkable(globalPosition(state, action, side))) 1f else -1f
			}
			scalarInput { _, state, action, side -> // Mine (presence and availability)
				state.getMineAt(globalPosition(state, action, side))?.let { mine ->
					val goldValue = mine.goldLeft inLinearRange 0f..3000f
					goldValue * if(mine.isAvailableFor(side, state)) 1f else -1f
				} ?: 0f
			}
			scalarInput { _, state, action, side -> // Base
				when(state.getBaseAt(globalPosition(state, action, side))?.owner)
				{
					side -> 1f
					side.opposite -> -1f
					else -> 0f
				}
			}
			oneOfNInput(Entity.Type.values().size + 1) { _, state, action, side -> // Entity (type)
				val types = state.getEntitiesAt(globalPosition(state, action, side)).map { it.type }.distinct()
				if(types.isNotEmpty()) types.single().ordinal.plus(1) else 0
			}
			scalarInput { _, state, action, side -> // Entity (hp)
				state.getEntitiesAt(globalPosition(state, action, side)).sumBy { it.hp } inLinearRange 0f..150f
			}
			scalarInput { _, state, action, side -> // Entity (max possible damage)
				state.getEntitiesAt(globalPosition(state, action, side))
						.sumBy { it.damage * it.actionPoints } inLinearRange 0f..50f
			}
			scalarInput { _, state, action, side -> // Is entity a victim of potential attack
				if((action as? EntityAttack)?.victim?.position == globalPosition(state, action, side)) 1f else 0f
			}
		}

		private fun InputContext.action()
		{
			oneOfNInput(5) { _, _, action, _ -> when(action)
			{
				is EntityAttack -> 0
				is BaseAttack -> 1
				is Move -> 2
				is Entrenchment -> 3
				is Recruitment -> 4
			} }
			actionExecutor()

			actionMove()
			actionRecruitment()
		}

		private fun InputContext.actionExecutor()
		{
			fun executorEntity(action: Action) = when(action)
			{
				is Attack -> action.attacker
				is Move -> action.entity
				is Entrenchment -> action.entity
				is Recruitment -> null
			}

			oneOfNInput(Entity.Type.values().size + 1) { _, _, action, _ ->
				executorEntity(action)?.type?.ordinal?.plus(1) ?: 0
			}
			scalarInput { _, _, action, _ -> (executorEntity(action)?.hp ?: 0) inLinearRange 0f..150f }
			scalarInput { _, _, action, _ -> (executorEntity(action)?.actionPoints ?: 0) inLinearRange 0f..10f }
			scalarInput { _, _, action, _ -> (executorEntity(action)?.rangeOfAttack ?: 0) inLinearRange 0f..5f }
			scalarInput { _, _, action, _ -> (executorEntity(action)?.damage ?: 0) inLinearRange 0f..50f }
		}

		private fun InputContext.actionMove()
		{
			oneOfNInput(Direction.values().size + 1) { _, _, action, _ ->
				(action as? Move)?.direction?.ordinal?.plus(1) ?: 0
			}
		}

		private fun InputContext.actionRecruitment()
		{
			fun recruitedEntity(action: Action) = (action as? Recruitment)?.entitySettings

			oneOfNInput(Entity.Type.values().size + 1) { _, _, action, _ ->
				recruitedEntity(action)?.type?.ordinal?.plus(1) ?: 0
			}
			scalarInput { _, _, action, _ -> (recruitedEntity(action)?.hp ?: 0) inLinearRange 0f..150f }
			scalarInput { _, _, action, _ -> (recruitedEntity(action)?.actionPoints ?: 0) inLinearRange 0f..10f }
			scalarInput { _, _, action, _ -> (recruitedEntity(action)?.rangeOfAttack ?: 0) inLinearRange 0f..5f }
			scalarInput { _, _, action, _ -> (recruitedEntity(action)?.damage ?: 0) inLinearRange 0f..50f }
			scalarInput { _, _, action, _ -> (recruitedEntity(action)?.cost ?: 0) inLinearRange 0f..500f }
		}

		private infix fun Int.inLinearRange(range: FloatRange) = this.toFloat().inLinearRange(range)

		private infix fun Float.inLinearRange(range: FloatRange) = (this - range.start) / range.size

		fun transformToInputArray(game: Game, state: GameState, action: Action, side: Player.Side) = FloatArray(size).also {
			root.write(it, 0, game, state, action, side)
		}
	}

	companion object
	{
		private fun createNetworkFromData(data: Data) =
				createNetworkSpecification().createNetwork(data.convertToNetworkData())

		private fun createNetworkRandomly(randomRange: FloatRange) =
				createNetworkSpecification().createNetworkRandomly(randomRange)

		private fun createNetworkSpecification() = reinforcementNetwork(Inputs.size) {
			reinforcementLayer {
				repeat(500) { reinforcementNeuron(TangensoidalActivation(1f), 0.1f) }
			}
			reinforcementLayer {
				repeat(50) { reinforcementNeuron(TangensoidalActivation(1f), 0.1f) }
			}
			reinforcementLayer {
				reinforcementNeuron(TangensoidalActivation(1f), 0.1f)
			}
		}

		private fun Data.convertToNetworkData() =
				NetworkData.fromList(layers.map { layer ->
					LayerData.fromList(layer.map { neuron ->
						NeuronData.fromArray(neuron)
					})
				})
	}

	override val data get() = Data(network.networkData.getLayersData().map { layer ->
		layer.getNeuronsData().map { neuron ->
			neuron.getWeights().toFloatArray()
		}
	})

	constructor(initialData: Data) : this(createNetworkFromData(initialData))

	constructor(randomRange: FloatRange) : this(createNetworkRandomly(randomRange))

	override fun evaluateAndGetAllData(game: Game, state: GameState, action: Action, side: Player.Side): DQNetwork.Evaluation
	{
		val input = Inputs.transformToInputArray(game, state, action, side)
		val outputs = network.calculateAndGetIntermediateOutputs(input)
		val finalOutput = outputs.last().single() // Network has 1 output
		return DQNetwork.Evaluation(input, outputs, finalOutput)
	}

	override fun calculateErrors(reward: Float, output: Float) =
			network.backpropagateErrorAndGetIntermediateErrors(floatArrayOf(reward - output)) // Network has 1 output

	override fun learn(evaluation: DQNetwork.Evaluation, allErrors: List<FloatArray>, learnRate: Float)
	{
		network.learn(evaluation.input, evaluation.allOutputs, allErrors, learnRate)
	}
}