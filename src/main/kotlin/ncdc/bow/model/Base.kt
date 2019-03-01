package ncdc.bow.model

import ncdc.bow.World

data class Base(val id: String,
                val owner: Owner,
                val position: LocalPosition,
                val hp: Int,
                val newEntity: Entity?)
{
	data class BaseData(val coordinates: ServerPosition? = null,
	                    val hp: Int = 0,
	                    val id: String? = null,
	                    val newUnit: Entity.EntityData? = null,
	                    val owner: Owner? = null)
	{
		fun toBase(world: World) = Base(id!!, owner!!, coordinates!!.toLocalSystem(world), hp, newUnit?.toEntity(world))
	}
}