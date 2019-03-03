package pl.karol202.bow.bot

import pl.karol202.bow.model.*

private fun <T> Iterable<T>.replace(old: T, new: T) = map { if(it == old) new else it }

interface Action
{
	//Performs action assuming it is possible
	fun perform(gameState: GameState): GameState

	fun toModelAction(): ActionModel
}

class Attack(private val attackerPlayer: Player,
             private val attacker: Entity,
             private val victimPlayer: Player,
             private val victim: Entity) : Action
{
	override fun perform(gameState: GameState): GameState
	{
		val newAttacker = attacker.copy(actionPoints = attacker.actionPoints - 1)
		val newVictim = victim.copy(hp = victim.hp - attacker.damage)

		val newAttackerPlayer = attackerPlayer.copy(entities = attackerPlayer.entities.replace(attacker, newAttacker))
		val newVictimPlayer = victimPlayer.copy(entities = victimPlayer.entities.replace(victim, newVictim))

		return when
		{
			attackerPlayer == gameState.player1 && victimPlayer == gameState.player2 ->
				gameState.copy(player1 = newAttackerPlayer, player2 = newVictimPlayer)
			attackerPlayer == gameState.player2 && victimPlayer == gameState.player1 ->
				gameState.copy(player1 = newVictimPlayer, player2 = newAttackerPlayer)
			else -> throw Exception("Invalid attacker - victim configuration")
		}
	}

	override fun toModelAction() = AttackActionModel(attacker, victim)
}

class Move(private val player: Player,
           private val entity: Entity,
           private val direction: Direction) : Action
{
	override fun perform(gameState: GameState): GameState
	{
		val newEntity = entity.copy(position = entity.position + direction.offset)
		val newPlayer = player.copy(entities = player.entities.replace(entity, newEntity))

		return when(player.side)
		{
			Player.Side.PLAYER1 -> gameState.copy(player1 = newPlayer)
			Player.Side.PLAYER2 -> gameState.copy(player2 = newPlayer)
		}
	}

	override fun toModelAction() = MoveActionModel(entity, direction)
}

class Entrenchment(private val player: Player,
                   private val entity: Entity) : Action
{
	override fun perform(gameState: GameState): GameState
	{
		val newEntity = entity.copy(entrench = true)
		val newPlayer = player.copy(entities = player.entities.replace(entity, newEntity))

		return when(player.side)
		{
			Player.Side.PLAYER1 -> gameState.copy(player1 = newPlayer)
			Player.Side.PLAYER2 -> gameState.copy(player2 = newPlayer)
		}
	}

	override fun toModelAction() = EntrenchActionModel(entity)
}

class Recruitment(private val player: Player,
                  private val entitySettings: Entity) : Action
{
	override fun perform(gameState: GameState): GameState
	{
		val newPlayer = player.copy(gold = player.gold - entitySettings.cost)
		return when(player.side)
		{
			Player.Side.PLAYER1 -> gameState.copy(player1 = newPlayer)
			Player.Side.PLAYER2 -> gameState.copy(player2 = newPlayer)
		}
	}

	override fun toModelAction() = RecruitActionModel(player.base, entitySettings.type)
}
