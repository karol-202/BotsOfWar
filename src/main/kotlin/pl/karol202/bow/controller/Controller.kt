package pl.karol202.bow.controller

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import pl.karol202.bow.bot.StandardBot
import pl.karol202.bow.game.StandardGameManager
import pl.karol202.bow.model.GameState

@RestController
class Controller
{
	private val gameManager = StandardGameManager(StandardBot())

	@PostMapping("/bot")
	fun updateGameState(@RequestBody gameState: GameState.GameStateData)
	{
		val orders = gameManager.updateGameStateAndGetOrders(gameState).map { it.toActionData() }
		//TODO Send orders
	}
}