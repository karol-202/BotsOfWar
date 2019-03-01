package ncdc.bow.model

import ncdc.bow.World

data class Mine(val id: String,
                val owner: Owner,
                val position: LocalPosition,
                val goldLeft: Int,
                val miningPerWorker: Int,
                val workersNumber: Int)
{
	data class MineData(val coordinates: ServerPosition? = null,
	                    val goldLeft: Int = 0,
	                    val id: String? = null,
	                    val miningPerWorker: Int = 0,
	                    val owner: Owner? = null,
	                    val workersNumber: Int = 0)
	{
		fun toMine(world: World) =
				Mine(id!!, owner!!, coordinates!!.toLocalSystem(world), goldLeft, miningPerWorker, workersNumber)
	}
}