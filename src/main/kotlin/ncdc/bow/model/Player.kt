package ncdc.bow.model

data class Player(val active: Boolean,
                  val base: Base,
                  val gold: Int,
                  val id: Int,
                  val owner: Owner,
                  val entities: List<Entity>)
{
	data class PlayerData(val active: Boolean = false,
	                      val base: Base.BaseData? = null,
	                      val gold: Int = 0,
	                      val id: Int = 0,
	                      val owner: Owner? = null,
	                      val units: List<Entity.EntityData>? = null)
	{
		fun toPlayer(world: World) = Player(active, base!!.toBase(world), gold, id, owner!!, units!!.map { it.toEntity(world) })
	}
}