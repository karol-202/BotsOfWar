package pl.karol202.bow.bot.darvin

import pl.karol202.bow.bot.*
import pl.karol202.bow.game.Game
import pl.karol202.bow.model.*

class DarvinBot(networkStructure: NetworkStructure) : Bot
{
	companion object
	{
		private const val ACTION_THRESHOLD = 0f
	}

	private val network = networkStructure.createNetwork()

	private lateinit var game: Game
	private lateinit var currentState: GameState
	private lateinit var side: Player.Side

	private val player get() = currentState.getPlayer(side)
	private val enemyPlayer get() = currentState.getPlayer(side.opposite)

	override fun play(game: Game, side: Player.Side): List<ActionModel>
	{
		initState(game, side)

		val actions = player.entities.flatMap { playWithEntity(it.id) } + recruit()
		return actions.filterNotNull().map { it.toModelAction() }
	}

	private fun initState(game: Game, side: Player.Side)
	{
		this.game = game
		this.currentState = game.gameState
		this.side = side
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
		fun GameMap.Cell.isWalkableFor(entityType: Entity.Type) =
				walkable || (this == GameMap.Cell.MINE && entityType == Entity.Type.WORKER)

		fun isMoveAvailable(offset: LocalPosition) = game.gameMap[entity.position + offset].isWalkableFor(entity.type)

		return Direction.values().filter { isMoveAvailable(it.offset) }.map { Move(player, entity, it) }
	}

	private fun getPossibleAttacks(entity: Entity): List<Attack>
	{
		val attacks = mutableListOf<Attack>()

		fun addAttacksFor(offset: LocalPosition): Boolean //True indicates possibility of further discovery
		{
			val position = entity.position + offset
			if(!game.gameMap[position.x, position.y].walkable) return false
			currentState.getEntitiesAt(position)
					.filter { it.owner == side.opposite }
					.forEach { attacks += Attack(player, entity, enemyPlayer, it) }
			return true
		}

		for(upOffset in 1..entity.rangeOfAttack)
			if(!addAttacksFor(LocalPosition(0, -upOffset))) break
		for(rightOffset in 1..entity.rangeOfAttack)
			if(!addAttacksFor(LocalPosition(0, rightOffset))) break
		for(downOffset in 1..entity.rangeOfAttack)
			if(!addAttacksFor(LocalPosition(0, downOffset))) break
		for(leftOffset in 1..entity.rangeOfAttack)
			if(!addAttacksFor(LocalPosition(0, -leftOffset))) break

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

	private fun evaluateAction(recruitment: Action): Float = TODO()
}
