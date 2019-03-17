package pl.karol202.bow.bot.environment

import pl.karol202.bow.model.Base
import pl.karol202.bow.model.GameState
import pl.karol202.bow.model.Player

class StandardEnvironment(private val mySide: Player.Side) : Environment
{
	companion object
	{
		private const val WIN_REWARD = 10f
		private const val LOSS_REWARD = -10f

		private const val MY_BASE_ATTACK_REWARD = -10f
		private const val ENEMY_BASE_ATTACK_REWARD = 10f

		private const val MY_ENTITY_KILL_REWARD = -0.7f
		private const val ENEMY_ENTITY_KILL_REWARD = 0.7f
	}

	private lateinit var lastState: GameState
	private lateinit var initialBases: Map<Player.Side, Base>

	override fun updateStateAndGetReward(newState: GameState, winner: Player.Side?): Float
	{
		if(!::lastState.isInitialized) init(newState)
		return getReward(newState, winner).also {
			lastState = newState
		}
	}

	private fun init(newState: GameState)
	{
		lastState = newState
		initialBases = newState.players.associateBy({ it.side }, { it.base })
	}

	private fun getReward(newState: GameState, winner: Player.Side?): Float
	{
		if(!::lastState.isInitialized) return 0f
		checkForWin(winner)?.let { return it }

		return newState.getRewardForBaseAttacks() + newState.getRewardForEntityKills()
	}

	private fun checkForWin(winner: Player.Side?) = winner?.let { if(it == mySide) WIN_REWARD else LOSS_REWARD }

	private fun GameState.getRewardForBaseAttacks() =
			Player.Side.values().map { getRewardForBaseAttack(it) }.sum()

	private fun GameState.getRewardForBaseAttack(side: Player.Side) =
			(getBaseHPLoss(side) / initialBases.getValue(side).hp) * getRewardForFullBaseAttack(side)

	private fun getRewardForFullBaseAttack(side: Player.Side) =
			if(side == mySide) MY_BASE_ATTACK_REWARD else ENEMY_BASE_ATTACK_REWARD

	private fun GameState.getBaseHPLoss(side: Player.Side) = lastState.getBaseHP(side) - this.getBaseHP(side)

	private fun GameState.getBaseHP(side: Player.Side) = getPlayer(side).base.hp

	private fun GameState.getRewardForEntityKills() = (lastState.entities - this.entities).map { getRewardForEntityKill(it.owner!!) }.sum()

	private fun getRewardForEntityKill(side: Player.Side) =
			if(side == mySide) MY_ENTITY_KILL_REWARD else ENEMY_ENTITY_KILL_REWARD
}