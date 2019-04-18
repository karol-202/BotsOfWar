package pl.karol202.bow.darvin.environment

import pl.karol202.bow.model.Base
import pl.karol202.bow.model.GameState
import pl.karol202.bow.model.Mine
import pl.karol202.bow.model.Player

class StandardEnvironment(private val mySide: Player.Side,
                          private val params: Params) : Environment
{
	data class Params(val baseReward: Float = 0f,
	                  val winReward: Float = 0f,
	                  val lossReward: Float = 0f,
	                  val myBaseAttackReward: Float = -10f,
	                  val enemyBaseAttackReward: Float = 10f,
	                  val myEntityKillReward: Float = -2f,
	                  val enemyEntityKillReward: Float = 2f,
	                  val entityRecruitReward: Float = 0.3f,
	                  val goldFindReward: Float = 0.2f) // For mining amount of gold specified in Mine.miningPerWorker (hardcoded 50)

	private val baseReward get() = params.baseReward
	private val winReward get() = params.winReward
	private val lossReward get() = params.lossReward
	private val myBaseAttackReward get() = params.myBaseAttackReward
	private val enemyBaseAttackReward get() = params.enemyBaseAttackReward
	private val myEntityKillReward get() = params.myEntityKillReward
	private val enemyEntityKillReward get() = params.enemyEntityKillReward
	private val entityRecruitReward get() = params.entityRecruitReward
	private val goldFindReward get() = params.goldFindReward

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

		return baseReward +
			   newState.getRewardForBaseAttacks() +
			   newState.getRewardForEntityKills() +
			   newState.getRewardForEntityRecruitment() +
			   newState.getRewardForGoldMining()
	}

	private fun checkForWin(winner: Player.Side?) = winner?.let { if(it == mySide) winReward else lossReward }

	private fun GameState.getRewardForBaseAttacks(): Float
	{
		fun GameState.getBaseHP(side: Player.Side) = getPlayer(side).base.hp

		fun getBaseHPLoss(side: Player.Side) = lastState.getBaseHP(side) - this.getBaseHP(side)

		fun getRewardForFullBaseAttack(side: Player.Side) =
				if(side == mySide) myBaseAttackReward else enemyBaseAttackReward

		fun getRewardForBaseAttack(side: Player.Side) =
				(getBaseHPLoss(side) / initialBases.getValue(side).hp) * getRewardForFullBaseAttack(side)

		return Player.Side.values().map { getRewardForBaseAttack(it) }.sum()
	}

	private fun GameState.getRewardForEntityKills(): Float
	{
		fun getRewardForEntityKill(side: Player.Side) =
				if(side == mySide) myEntityKillReward else enemyEntityKillReward

		val currentIds = allEntities.map { it.id }
		val difference = lastState.allEntities.filter { it.id !in currentIds }
		return difference.map { getRewardForEntityKill(it.owner!!) }.sum()
	}

	private fun GameState.getRewardForEntityRecruitment(): Float
	{
		val previousIds = lastState.getPlayer(mySide).entities.map { it.id }
		val difference = getPlayer(mySide).entities.filterNot { it.id in previousIds }
		return difference.size * entityRecruitReward
	}

	private fun GameState.getRewardForGoldMining(): Float
	{
		fun isMineMine(mine: Mine) = mine.getOwner(this) == mySide

		fun getGoldLoss(mine: Mine) = lastState.getMineById(mine.id).goldLeft - getMineById(mine.id).goldLeft

		fun getRewardForMiningIn(mine: Mine) = (getGoldLoss(mine).toFloat() / mine.miningPerWorker) * goldFindReward

		return mines.filter { isMineMine(it) }.map { getRewardForMiningIn(it) }.sum()
	}
}