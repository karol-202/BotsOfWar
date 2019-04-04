package pl.karol202.bow.model

import org.springframework.web.client.RestTemplate
import pl.karol202.bow.game.Game
import pl.karol202.bow.service.GameService

object GameResult
{
	data class GameResultData(val gameId: Int = 0,
	                          val winnerId: Int = 0)

	fun checkWinner(game: Game) = RestTemplate().getForObject(getEndpoint(game), GameResultData::class.java)
											    ?.takeIf { it.gameId == game.id }
											    ?.let { game.state.getPlayerById(it.winnerId).side }

	private fun getEndpoint(game: Game) = "${GameService.serverAddress}/getResultGame/${game.id}"
}