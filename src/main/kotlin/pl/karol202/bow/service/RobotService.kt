package pl.karol202.bow.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import pl.karol202.bow.controller.BotController
import pl.karol202.bow.model.NewGameModel
import pl.karol202.bow.util.Counter
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy
import kotlin.random.Random

@Service
class RobotService @Autowired constructor(private val gameService: GameService,
                                          private val dataService: DataService)
{
	companion object
	{
		var localAddress = "http://172.17.0.1:4321"

		private const val GAME_PREFIX_STANDARD = 100_000_000
		private const val GAME_PREFIX_TEACHING = 200_000_000

		private const val STANDARD_GAME_COUNTER_MAX = 99_999_999
		private const val TEACHING_CYCLE_COUNTER_MAX = 999
		private const val TEACHING_EPOCH_COUNTER_MAX = 99_999
	}

	data class Params(val learnRate: Float = 0.0001f,
	                  val samplesPerEpoch: Int = 500)

	data class Data(val standardGameCounter: Counter = Counter(maxValue = STANDARD_GAME_COUNTER_MAX),
	                val teachingCycleCounter: Counter = Counter(maxValue = TEACHING_CYCLE_COUNTER_MAX),
	                val teachingEpochCounter: Counter = Counter(maxValue = TEACHING_EPOCH_COUNTER_MAX))

	/*
	Game id structure:
	- standard game:
	  1         - standard game prefix
	   xxxxxxxx - game counter (incremented before game), 0-99999999

	- teaching:
	  2         - teaching prefix
	   xxx      - teaching cycle counter (incremented manually), 0-999
	      xxxxx - teaching epoch, 0-99999
	 */

	private val coroutineJob = Job()
	private val coroutineScope = CoroutineScope(coroutineJob)

	private val logger: Logger = LoggerFactory.getLogger(javaClass)

	var params: Params = dataService.loadRobotParams() ?: Params()
		@Synchronized set(value)
		{
			field = value
			saveParams()
		}
	private var data: Data = dataService.loadRobotData() ?: Data()
		set(value)
		{
			field = value
			saveData()
		}
	private var teaching = false

	private val learnRate get() = params.learnRate
	private val samplesPerEpoch get() = params.samplesPerEpoch

	private val standardGameCounter get() = data.standardGameCounter
	private val teachingCycleCounter get() = data.teachingCycleCounter
	private val teachingEpochCounter get() = data.teachingEpochCounter

	private val newGameEndpoint get() = "${GameService.serverAddress}/newGame"
	private val botEndpoint get() = "$localAddress/${BotController.ENDPOINT}"

	private val standardGameId get() = GAME_PREFIX_STANDARD + standardGameCounter.value
	private val teachingGameId get() = GAME_PREFIX_TEACHING +
										    (teachingCycleCounter.value * 100000) +
										    teachingEpochCounter.value

	@PostConstruct
	fun onStart()
	{
		logger.debug("RobotService created")
	}

	@Synchronized
	fun newGame()
	{
		data = data.copy(standardGameCounter = standardGameCounter.next)
		newGame(standardGameId)
	}

	//Blocks until the end of the game
	private fun newGame(gameId: Int)
	{
		val newGameModel = NewGameModel(gameId, botEndpoint, Random.nextInt(), botEndpoint, Random.nextInt(), false)
		RestTemplate().postForObject(newGameEndpoint, newGameModel, String::class.java)
	}

	@Synchronized
	fun initTeaching(bot1Name: String, bot2Name: String, bot1WeightRange: Float, bot2WeightRange: Float)
	{
		data = data.copy(teachingCycleCounter = teachingCycleCounter.next,
		                 teachingEpochCounter = teachingEpochCounter.zero)

		val directoryName = "teach_${teachingCycleCounter.value}"
		gameService.params = gameService.params.copy(botsDirectory = directoryName, samplesDirectory = directoryName)

		gameService.addNewBot(bot1Name, bot1WeightRange)
		gameService.addNewBot(bot2Name, bot2WeightRange)
	}

	@Synchronized
	fun startTeaching()
	{
		coroutineScope.launch { teachInALoop() }
	}

	// Not synchronized in order to be able to stop when synchronized teachInLoop() is being executed
	fun stopTeaching()
	{
		teaching = false
	}

	@Synchronized // Synchronized because it's entry point for coroutine
	private fun teachInALoop()
	{
		logger.info("Teaching started")
		teaching = true
		while(teaching) teachEpoch()
		logger.info("Teaching stopped")
	}

	private fun teachEpoch()
	{
		data = data.copy(teachingEpochCounter = teachingEpochCounter.next)

		logger.info("Teaching epoch started: ${teachingEpochCounter.value}")
		teachOnce()
		logger.info("Teaching epoch ended: ${teachingEpochCounter.value}")
	}

	private fun teachOnce()
	{
		var gameEnded = false
		gameService.onGameEndListener = { gameEnded = true }
		newGame(teachingGameId)
		while(!gameEnded) Thread.yield()
		gameService.onGameEndListener = null
		gameService.teach(learnRate, samplesPerEpoch, null)
	}

	private fun saveParams() = dataService.saveRobotParams(params)

	private fun saveData() = dataService.saveRobotData(data)

	@PreDestroy
	fun onDestroy() = coroutineJob.cancel()
}