package ncdc.bow.model

import org.springframework.web.client.RestTemplate

data class GameSettings(val entitySettings: Map<Entity.Type, Entity>,
                        val miningPerTurn: Int,
                        val numberOfResources: Int)
{
	private data class GameSettingsData(val archer: Entity.EntityData? = null,
	                                    val horse: Entity.EntityData? = null,
	                                    val warrior: Entity.EntityData? = null,
	                                    val worker: Entity.EntityData? = null,
	                                    val miningPerTurn: Int = 0,
	                                    val numberOfResources: Int = 0)
	{
		fun toGameSettings(world: World) =
				GameSettings(mapOf(Entity.Type.ARCHER to archer!!.toEntity(world),
				                   Entity.Type.HORSE to horse!!.toEntity(world),
				                   Entity.Type.WARRIOR to warrior!!.toEntity(world),
				                   Entity.Type.WORKER to worker!!.toEntity(world)), miningPerTurn, numberOfResources)
	}

	companion object
	{
		private const val ENDPOINT = "http://bow.westeurope.cloudapp.azure.com:8080/getGameSettings"

		fun fromServer(world: World) =
				RestTemplate().getForObject(ENDPOINT, GameSettingsData::class.java)?.toGameSettings(world)
	}
}