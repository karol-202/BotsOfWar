package pl.karol202.bow.model

import pl.karol202.bow.game.Game

data class Mine(val id: String,
                val position: LocalPosition,
                val goldLeft: Int,
                val miningPerWorker: Int,
                val workersNumber: Int)
{
	data class MineData(val coordinates: ServerPosition? = null,
	                    val goldLeft: Int = 0,
	                    val id: String? = null,
	                    val miningPerWorker: Int = 0,
	                    val workersNumber: Int = 0)
	{
		fun toMine(game: Game) = Mine(id!!, coordinates!!.toLocalSystem(game), goldLeft, miningPerWorker, workersNumber)
	}

	fun isAvailableFor(side: Player.Side, state: GameState) = getOwner(state) != side.opposite

	fun getOwner(state: GameState) = state.getEntitiesAt(position).map { it.owner }.distinct().singleOrNull()
}