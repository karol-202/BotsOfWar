package ncdc.bow.model

data class Base(val position: LocalPosition,
                val hp: Int,
                val id: String,
                val newEntity: Entity?,
                val owner: Owner)
{
	data class BaseData(val coordinates: ServerPosition? = null,
	                    val hp: Int = 0,
	                    val id: String? = null,
	                    val newUnit: Entity.EntityData? = null,
	                    val owner: Owner? = null)
	{
		fun toBase(world: World) = Base(coordinates!!.toLocalSystem(world), hp, id!!, newUnit?.toEntity(world), owner!!)
	}
}