package pl.karol202.bow.model

import pl.karol202.bow.game.Game

data class Mine(val id: String,
                val owner: Owner,
                val position: LocalPosition,
                val goldLeft: Int,
                val miningPerWorker: Int,
                val workersNumber: Int)
{
	enum class Owner(val correspondingSide: Player.Side?)
	{
		PLAYER1(Player.Side.PLAYER1),
		PLAYER2(Player.Side.PLAYER2),
		NEUTRAL(null)
	}

	data class MineData(val coordinates: ServerPosition? = null,
	                    val goldLeft: Int = 0,
	                    val id: String? = null,
	                    val miningPerWorker: Int = 0,
	                    val owner: Owner? = null,
	                    val workersNumber: Int = 0)
	{
		fun toMine(game: Game) = Mine(id!!, owner!!, coordinates!!.toLocalSystem(game), goldLeft, miningPerWorker, workersNumber)
	}

	fun isAvailableFor(side: Player.Side) = owner.correspondingSide != side.opposite
}