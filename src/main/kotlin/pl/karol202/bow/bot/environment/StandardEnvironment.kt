package pl.karol202.bow.bot.environment

import pl.karol202.bow.model.Base
import pl.karol202.bow.model.GameState
import pl.karol202.bow.model.Mine
import pl.karol202.bow.model.Player

class StandardEnvironment(private val mySide: Player.Side) : Environment
{
	companion object
	{
		private const val WIN_REWARD = 1f
		private const val LOSS_REWARD = -1f

		private const val MY_BASE_ATTACK_REWARD = -10f
		private const val ENEMY_BASE_ATTACK_REWARD = 10f

		private const val MY_ENTITY_KILL_REWARD = -0.5f
		private const val ENEMY_ENTITY_KILL_REWARD = 0.5f

		private const val GOLD_FIND_REWARD = 0.02f // For mining amount of gold specified in GameSettings.miningPerTurn
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
		checkForWin(winner)?.let { return it }

		return newState.getRewardForBaseAttacks() + newState.getRewardForEntityKills() + newState.getRewardForGoldMining()
	}

	private fun checkForWin(winner: Player.Side?) = winner?.let { if(it == mySide) WIN_REWARD else LOSS_REWARD }

	private fun GameState.getRewardForBaseAttacks(): Float
	{
		fun GameState.getBaseHP(side: Player.Side) = getPlayer(side).base.hp

		fun getBaseHPLoss(side: Player.Side) = lastState.getBaseHP(side) - this.getBaseHP(side)

		fun getRewardForFullBaseAttack(side: Player.Side) =
				if(side == mySide) MY_BASE_ATTACK_REWARD else ENEMY_BASE_ATTACK_REWARD

		fun getRewardForBaseAttack(side: Player.Side) =
				(getBaseHPLoss(side) / initialBases.getValue(side).hp) * getRewardForFullBaseAttack(side)

		return Player.Side.values().map { getRewardForBaseAttack(it) }.sum()
	}

	private fun GameState.getRewardForEntityKills(): Float
	{
		fun getRewardForEntityKill(side: Player.Side) =
				if(side == mySide) MY_ENTITY_KILL_REWARD else ENEMY_ENTITY_KILL_REWARD

		val currentIds = allEntities.map { it.id }
		val difference = lastState.allEntities.filterNot { it.id in currentIds }
		return difference.map { getRewardForEntityKill(it.owner!!) }.sum()
	}

	private fun GameState.getRewardForGoldMining(): Float
	{
		fun Mine.isMineMine() = owner.correspondingSide == mySide

		fun getGoldLoss(mine: Mine) = lastState.getMineById(mine.id).goldLeft - getMineById(mine.id).goldLeft

		fun getRewardForMiningIn(mine: Mine) = (getGoldLoss(mine).toFloat() / mine.miningPerWorker) * GOLD_FIND_REWARD

		return mines.filter { it.isMineMine() }.map { getRewardForMiningIn(it) }.sum()
	}
}