package ncdc.bow

import org.springframework.web.client.RestTemplate

data class Entity(val id: String,
                  val position: LocalPosition,
                  val owner: String?,
                  val hp: Int,
                  val type: Type?,
                  val rangeOfAttack: Int,
                  val actionPoints: Int,
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

	private data class EntityData(val id: String = "",
	                              val coordinates: ServerPosition = ServerPosition(),
	                              val owner: String? = null,
	                              val hp: Int = 0,
	                              val name: Type? = null,
	                              val rangeOfAttack: Int = 0,
	                              val actionPoints: Int = 0,
	                              val damage: Int = 0,
	                              val cost: Int = 0,
	                              val entrench: Boolean = false)
	{
		fun toEntity(world: World) =
				Entity(id, coordinates.toLocalSystem(world), owner, hp, name, rangeOfAttack, actionPoints, damage, cost, entrench)
	}

	companion object
	{
		private const val ENDPOINT = "http://bow.westeurope.cloudapp.azure.com:8080/getUnitList"

		fun allFromServer(world: World) =
				RestTemplate().getForObject(ENDPOINT, Array<EntityData>::class.java)?.map { it.toEntity(world) }
						?: throw APIException("Cannot fetch entities")
	}
}