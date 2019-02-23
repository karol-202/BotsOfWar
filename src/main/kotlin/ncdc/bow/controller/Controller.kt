package ncdc.bow.controller

import ncdc.bow.model.GameState
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class Controller
{
	@PostMapping("/bot")
	fun updateGameState(@RequestBody gameState: GameState.GameStateData)
	{
		println(gameState)
	}
}