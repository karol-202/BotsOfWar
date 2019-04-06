package pl.karol202.bow.robot

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import pl.karol202.bow.service.GameService

@Component
class Robot
{
	@Autowired
	private lateinit var gameService: GameService
	private val logger: Logger = LoggerFactory.getLogger(javaClass)

	//Main entry point for robot
	fun start()
	{

	}
}