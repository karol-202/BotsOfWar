package pl.karol202.bow.darvin.environment

import pl.karol202.bow.model.Base
import pl.karol202.bow.model.GameState
import pl.karol202.bow.model.Mine
import pl.karol202.bow.model.Player

class StandardEnvironment(private val mySide: Player.Side) : Environment
{
	companion object
	{
		//Received every turn
		private const val BASE_REWARD = -0.1f

		private const val WIN_REWARD = 0f
		private const val LOSS_REWARD = 0f

		private const val MY_BASE_ATTACK_REWARD = -10f
		private const val ENEMY_BASE_ATTACK_REWARD = 10f

		private const val MY_ENTITY_KILL_REWARD = -2f
		private const val ENEMY_ENTITY_KILL_REWARD = 2f

		private const val ENTITY_RECRUIT_REWARD = 0.3f

		private const val GOLD_FIND_REWARD = 0.2f // For mining amount of gold specified in Mine.miningPerWorker (hardcoded 50)
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

		return BASE_REWARD +
			   newState.getRewardForBaseAttacks() +
			   newState.getRewardForEntityKills() +
			   newState.getRewardForEntityRecruitment() +
			   newState.getRewardForGoldMining()
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

	private fun GameState.getRewardForEntityRecruitment(): Float
	{
		val previousIds = lastState.getPlayer(mySide).entities.map { it.id }
		val difference = getPlayer(mySide).entities.filterNot { it.id in previousIds }
		return difference.size * ENTITY_RECRUIT_REWARD
	}

	private fun GameState.getRewardForGoldMining(): Float
	{
		fun isMineMine(mine: Mine) = mine.getOwner(this) == mySide

		fun getGoldLoss(mine: Mine) = lastState.getMineById(mine.id).goldLeft - getMineById(mine.id).goldLeft

		fun getRewardForMiningIn(mine: Mine) = (getGoldLoss(mine).toFloat() / mine.miningPerWorker) * GOLD_FIND_REWARD

		return mines.filter { isMineMine(it) }.map { getRewardForMiningIn(it) }.sum()
	}
}