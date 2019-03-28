package pl.karol202.bow.robot

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import pl.karol202.bow.bot.neural.DarvinReinforcementNetwork
import pl.karol202.bow.game.DarvinGameListener
import pl.karol202.bow.service.GameService

@Component
class Robot : DarvinGameListener
{
	@Autowired
	private lateinit var gameService: GameService

	private val logger = LoggerFactory.getLogger(Robot::class.java)

	//Main entry point for robot
	fun start()
	{
		gameService.setGameListener(this)
		//runSingleGame()
	}

	private fun runSingleGame()
	{

	}

	override fun onGameStart()
	{
		logger.info("Game started")
	}

	override fun onUpdate(turn: Int)
	{
		logger.info("Game updated (turn: $turn)")
	}

	override fun onReset()
	{
		logger.warn("Game restarted without teaching")
	}

	override fun onStopAndTeach(data: DarvinReinforcementNetwork.Data?)
	{
		logger.info("Teaching done")
		data?.let { gameService.dataSerializer.saveData(it) }
	}
}