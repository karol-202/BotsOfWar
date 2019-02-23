package ncdc.bow.model

data class Mine(val position: LocalPosition,
                val goldLeft: Int,
                val id: String,
                val miningPerWorker: Int,
                val owner: Owner,
                val workersNumber: Int)
{
	data class MineData(val coordinates: ServerPosition? = null,
	                    val goldLeft: Int = 0,
	                    val id: String? = null,
	                    val miningPerWorker: Int = 0,
	                    val owner: Owner? = null,
	                    val workersNumber: Int = 0)
	{
		fun toMine(world: World) = Mine(coordinates!!.toLocalSystem(world), goldLeft, id!!, miningPerWorker, owner!!, workersNumber)
	}
}