package pl.karol202.bow.bot

import pl.karol202.bow.bot.agent.Agent
import pl.karol202.bow.bot.environment.Environment
import pl.karol202.bow.game.Game
import pl.karol202.bow.model.*

class DarvinBot(private val agent: Agent,
                private val environment: Environment) : Bot
{
	companion object
	{
		private const val ACTION_THRESHOLD = 0f
	}

	private lateinit var game: Game
	private lateinit var currentState: GameState
	private lateinit var side: Player.Side

	private val player get() = currentState.getPlayer(side)
	private val enemyPlayer get() = currentState.getPlayer(side.opposite)

	override fun play(game: Game, side: Player.Side): Order
	{
		initState(game, side)
		receiveRewards(game.state, null)

		val actions = player.entities.flatMap { playWithEntity(it.id) } + recruit()
		val actionModels = actions.filterNotNull().map { it.toModelAction() }
		return Order(actionModels)
	}

	private fun initState(game: Game, side: Player.Side)
	{
		this.game = game
		this.currentState = game.state
		this.side = side
	}

	private fun receiveRewards(state: GameState, winner: Player.Side?)
	{
		agent.receiveReward(environment.updateStateAndGetReward(state, winner))
		agent.moveToNextTimestamp()
	}

	private fun playWithEntity(entityId: String): List<Action>
	{
		val actions = mutableListOf<Action>()

		var entity = currentState.getEntityById(entityId, player)
		while(entity.actionPoints > 0)
		{
			val action = playOnceWithEntity(entity)
			if(action != null) actions += action
			else break //If no action has been performed now, continuing would cause an infinite loop
			entity = currentState.getEntityById(entityId, player)
		}

		return actions
	}

	private fun playOnceWithEntity(entity: Entity): Action?
	{
		val possibilities = getPossibleMoves(entity) + getPossibleAttacks(entity) + getPossibleEntrenchment(entity)
		val action = possibilities.associateWith { evaluateAction(it) }.maxBy { it.value }
				?.takeIf { it.value >= ACTION_THRESHOLD }?.key
		return action?.also { currentState = action.perform(currentState) }
	}

	private fun getPossibleMoves(entity: Entity): List<Move>
	{
		fun isCellWalkable(position: LocalPosition) = game.isPositionWalkable(position)

		fun isCellOccupied(position: LocalPosition) = currentState.getEntitiesAt(position).isNotEmpty()

		fun isCellAvailable(position: LocalPosition) = isCellWalkable(position) && !isCellOccupied(position)

		fun isCellAnAvailableMine(position: LocalPosition) = currentState.getMineAt(position)?.isAvailableFor(side) == true

		fun isCellAMineAvailableFor(entityType: Entity.Type, position: LocalPosition) =
				entityType == Entity.Type.WORKER && isCellAnAvailableMine(position)

		fun isMoveAvailable(position: LocalPosition) =
				isCellAvailable(position) || isCellAMineAvailableFor(entity.type, position)

		return Direction.values().filter { isMoveAvailable(entity.position + it.offset) }.map { Move(player, entity, it) }
	}

	private fun getPossibleAttacks(entity: Entity): List<Attack>
	{
		val attacks = mutableListOf<Attack>()

		fun getEntityAttacksFor(position: LocalPosition) = currentState.getEntitiesAt(position)
				.filter { it.owner == side.opposite }
				.map { EntityAttack(player, entity, enemyPlayer, it) }

		fun getBaseAttacksFor(position: LocalPosition) =
			if(enemyPlayer.base.position == position) listOf(BaseAttack(player, entity, enemyPlayer)) else emptyList()

		fun addAttacksFor(offset: LocalPosition): Boolean //True indicates possibility of further discovery
		{
			val position = entity.position + offset
			if(game.gameMap[position]?.walkable != true) return false

			attacks += getEntityAttacksFor(position)
			attacks += getBaseAttacksFor(position)
			return true
		}

		Direction.values().forEach { direction ->
			for(distance in 1..entity.rangeOfAttack)
				if(!addAttacksFor(direction.offset * distance)) break
		}
		return attacks
	}

	private fun getPossibleEntrenchment(entity: Entity): List<Entrenchment> =
			if(!entity.entrench) listOf(Entrenchment(player, entity))
			else emptyList()


	private fun recruit(): Recruitment?
	{
		val recruitment = getPossibleRecruitments().associateWith { evaluateAction(it) }.maxBy { it.value }
				?.takeIf { it.value >= ACTION_THRESHOLD }?.key
		return recruitment?.also { currentState = it.perform(currentState) }
	}

	private fun getPossibleRecruitments() =
			if(!isBaseOccupied()) game.entitySettings.filter { player.gold >= it.cost }.map { Recruitment(player, it) }
			else emptyList()

	private fun isBaseOccupied() = currentState.getEntitiesAt(player.base.position).isNotEmpty()

	private fun evaluateAction(action: Action): Float = agent.evaluateAction(currentState, action)

	override fun endGame(game: Game, winner: Player.Side)
	{
		receiveRewards(game.state, winner)
		agent.teachAllAndReset()
	}
}
