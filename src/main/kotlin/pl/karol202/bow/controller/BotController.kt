package pl.karol202.bow.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pl.karol202.bow.model.GameState
import pl.karol202.bow.service.GameService

@RestController
class BotController
{
	companion object
	{
		const val SERVER_ADDRESS = "http://192.168.99.100:8080"
	}

	@Autowired
	private lateinit var gameService: GameService

	@RequestMapping("/bot")
	fun updateGameState(@RequestBody gameState: GameState.GameStateData) =
			gameService.updateStateAndGetOrder(gameState).toOrderData()
}