package ncdc.bow.model

import ncdc.bow.World

data class Entity(val id: String,
                  val owner: Owner?,
                  val type: Type,
                  val position: LocalPosition,
                  val hp: Int,
                  val actionPoints: Int,
                  val rangeOfAttack: Int,
                  val damage: Int,
                  val cost: Int,
                  val entrench: Boolean)
{
	enum class Type
	{
		HORSE,
		ARCHER,
		WORKER,
		WARRIOR
	}

	data class EntityData(val id: String = "",
	                      val coordinates: ServerPosition? = null,
	                      val owner: Owner? = null,
	                      val hp: Int = 0,
	                      val name: Type? = null,
	                      val rangeOfAttack: Int = 0,
	                      val actionPoints: Int = 0,
	                      val damage: Int = 0,
	                      val cost: Int = 0,
	                      val entrench: Boolean = false)
	{
		fun toEntity(world: World) =
				Entity(id, owner, name!!, coordinates?.toLocalSystem(world) ?: LocalPosition(),
				       hp, actionPoints, rangeOfAttack, damage, cost, entrench)
	}
}