package pl.karol202.bow.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pl.karol202.bow.model.Player
import pl.karol202.bow.service.GameService

@RestController
class AdminController
{
	@Autowired
	private lateinit var gameService: GameService

	@RequestMapping("/admin/removeAgentData")
	fun removeAgentData(side: Player.Side) = gameService.removeAgentData(side)
}