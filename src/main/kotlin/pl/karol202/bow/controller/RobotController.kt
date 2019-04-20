package pl.karol202.bow.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import pl.karol202.bow.service.RobotService

@RestController
class RobotController
{
	@Autowired
	private lateinit var robotService: RobotService

	@PostMapping("/robot/startGame")
	fun startGame() = robotService.startGameOnce()
}