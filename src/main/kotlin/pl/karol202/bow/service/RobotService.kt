package pl.karol202.bow.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import pl.karol202.bow.controller.BotController
import pl.karol202.bow.model.NewGameModel
import pl.karol202.bow.util.Counter
import javax.annotation.PostConstruct
import kotlin.random.Random

@Service
class RobotService
{
	companion object
	{
		var localAddress = "http://172.17.0.1:4321"

		private const val GAME_PREFIX_STANDARD = 1 * 1000000

		private const val STANDARD_GAME_COUNTER_MAX = 999999
	}

	data class Data(var standardGameCounter: Counter = Counter(maxValue = STANDARD_GAME_COUNTER_MAX))

	/*
	Game id structure:
	- standard game:
	  1       - standard game prefix
	   xxxxxx - game counter (incremented after game)
	 */

	@Autowired
	private lateinit var gameService: GameService
	@Autowired
	private lateinit var dataService: DataService
	private val logger: Logger = LoggerFactory.getLogger(javaClass)

	private lateinit var data: Data

	private val newGameEndpoint get() = "${GameService.serverAddress}/newGame"
	private val botEndpoint get() = "$localAddress/${BotController.ENDPOINT}"

	private val nextStandardGameId get() = GAME_PREFIX_STANDARD + data.standardGameCounter.value.also { saveData() }

	//Main entry point for robot
	@PostConstruct
	fun onStart()
	{
		data = dataService.loadRobotData() ?: Data()

		logger.debug("RobotService created")
	}

	@Synchronized
	fun startGameOnce() = startGame(nextStandardGameId, Random.nextInt(), Random.nextInt())

	private fun startGame(gameId: Int, player1Id: Int, player2Id: Int)
	{
		val newGameModel = NewGameModel(gameId, botEndpoint, player1Id, botEndpoint, player2Id, false)
		RestTemplate().postForObject(newGameEndpoint, newGameModel, String::class.java)
	}

	private fun saveData() = dataService.saveRobotData(data)
}