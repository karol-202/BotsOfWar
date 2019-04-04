package pl.karol202.bow.model

import org.springframework.web.client.RestTemplate
import pl.karol202.bow.game.Game
import pl.karol202.bow.service.GameService
import pl.karol202.bow.util.APIException

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
		fun toGameSettings(game: Game) =
				GameSettings(mapOf(Entity.Type.ARCHER to archer!!.toEntity(game),
				                   Entity.Type.HORSE to horse!!.toEntity(game),
				                   Entity.Type.WARRIOR to warrior!!.toEntity(game),
				                   Entity.Type.WORKER to worker!!.toEntity(game)), miningPerTurn, numberOfResources)
	}

	companion object
	{
		private val endpoint get() = "${GameService.serverAddress}/getGameSettings"

		fun fromServer(game: Game) =
				RestTemplate().getForObject(endpoint, GameSettingsData::class.java)?.toGameSettings(game)
						?: throw APIException("Cannot fetch game settings.")
	}
}