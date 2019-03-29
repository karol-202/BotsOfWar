package pl.karol202.bow.darvin

import pl.karol202.bow.model.*
import kotlin.math.floor

private fun <T> Iterable<T>.replace(old: T, new: T?) =
		if(new != null) map { if(it == old) new else it }
		else remove(old)

private fun <T> Iterable<T>.remove(element: T) = filterNot { it == element }

interface Action
{
	//Performs action assuming it is possible
	fun perform(gameState: GameState): GameState

	fun toModelAction(): ActionModel
}

sealed class Attack(private val attackerPlayer: Player,
                      val attacker: Entity,
                      protected val victimPlayer: Player) : Action
{
	override fun perform(gameState: GameState): GameState
	{
		val newAttackerPlayer = copyAttackerPlayer()
		val newVictimPlayer = copyVictimPlayer()

		return when
		{
			attackerPlayer == gameState.player1 && victimPlayer == gameState.player2 ->
				gameState.copy(player1 = newAttackerPlayer, player2 = newVictimPlayer)
			attackerPlayer == gameState.player2 && victimPlayer == gameState.player1 ->
				gameState.copy(player1 = newVictimPlayer, player2 = newAttackerPlayer)
			else -> throw Exception("Invalid attacker - victim configuration")
		}
	}

	private fun copyAttackerPlayer(): Player
	{
		val newAttacker = attacker.copy(actionPoints = attacker.actionPoints - 1)
		return attackerPlayer.copy(entities = attackerPlayer.entities.replace(attacker, newAttacker))
	}

	protected abstract fun copyVictimPlayer(): Player
}

class EntityAttack(attackerPlayer: Player,
                   attacker: Entity,
                   victimPlayer: Player,
                   val victim: Entity) : Attack(attackerPlayer, attacker, victimPlayer)
{
	override fun copyVictimPlayer(): Player
	{
		val damageMultiplier = if(victim.entrench) 0.5f else 1f
		val newVictimHp = victim.hp - floor(attacker.damage * damageMultiplier).toInt() //Not sure how to round
		val newVictim = if(newVictimHp > 0) victim.copy(hp = newVictimHp) else null
		return victimPlayer.copy(entities = victimPlayer.entities.replace(victim, newVictim))
	}

	override fun toModelAction() = AttackActionModel(attacker, victim)
}

class BaseAttack(attackerPlayer: Player,
                 attacker: Entity,
                 victimPlayer: Player) : Attack(attackerPlayer, attacker, victimPlayer)
{
	override fun copyVictimPlayer(): Player
	{
		val oldBase = victimPlayer.base
		val newBase = oldBase.copy(hp = oldBase.hp - attacker.damage)
		return victimPlayer.copy(base = newBase)
	}

	override fun toModelAction() = AttackActionModel(attacker, victimPlayer.base)
}

class Move(private val player: Player,
           val entity: Entity,
           val direction: Direction) : Action
{
	override fun perform(gameState: GameState): GameState
	{
		val newEntity = entity.copy(position = entity.position + direction.offset,
		                            actionPoints = entity.actionPoints - 1)
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
                   val entity: Entity) : Action
{
	override fun perform(gameState: GameState): GameState
	{
		val newEntity = entity.copy(entrench = true,
		                            actionPoints = entity.actionPoints - 1)
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
                  val entitySettings: Entity) : Action
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
