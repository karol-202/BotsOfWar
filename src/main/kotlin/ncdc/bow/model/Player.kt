package ncdc.bow.model

import ncdc.bow.World

data class Player(val id: Int,
                  val owner: Owner,
                  val active: Boolean,
                  val base: Base,
                  val gold: Int,
                  val entities: List<Entity>)
{
	data class PlayerData(val active: Boolean = false,
	                      val base: Base.BaseData? = null,
	                      val gold: Int = 0,
	                      val id: Int = 0,
	                      val owner: Owner? = null,
	                      val units: List<Entity.EntityData>? = null)
	{
		fun toPlayer(world: World) =
				Player(id, owner!!, active, base!!.toBase(world), gold, units!!.map { it.toEntity(world) })
	}
}