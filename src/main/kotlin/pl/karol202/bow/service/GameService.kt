package pl.karol202.bow.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import pl.karol202.bow.darvin.agent.DQNAgent
import pl.karol202.bow.darvin.bot.DarvinBot
import pl.karol202.bow.darvin.environment.StandardEnvironment
import pl.karol202.bow.darvin.neural.AxonDQNetwork
import pl.karol202.bow.darvin.neural.DQNetwork
import pl.karol202.bow.game.DarvinGameListener
import pl.karol202.bow.game.StandardGameManager
import pl.karol202.bow.model.GameState
import pl.karol202.bow.model.Order
import pl.karol202.bow.model.Player
import pl.karol202.bow.util.DataSerializer
import pl.karol202.bow.util.randomOrNull
import java.text.SimpleDateFormat
import java.util.*
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy
import kotlin.system.measureTimeMillis

private typealias DarvinBotWithDQNAgent = DarvinBot<DQNAgent<AxonDQNetworkData>, DQNAgentData>

typealias DarvinBotData = DarvinBot.Data<DQNAgentData>

typealias DQNAgentData = DQNAgent.Data<AxonDQNetworkData>

typealias AxonDQNetworkData = AxonDQNetwork.Data

@Service
class GameService : DarvinGameListener<DarvinBotWithDQNAgent>
{
	companion object
	{
		var serverAddress = "http://localhost:8080"
	}

	data class Params(val actionThreshold: Float,
	                  val learnRate: Float,
	                  val discountFactor: Float,
	                  val epsilon: Float,
	                  val learningSamplesLimit: Int,
	                  val botsDirectory: String,
	                  val samplesDirectory: String,
	                  val allowBotDuplication: Boolean,
	                  val botBindings: Map<Player.Side, String>)
	{
		constructor(gameService: GameService) : this(gameService.actionThreshold,
		                                             gameService.learnRate,
		                                             gameService.discountFactor,
		                                             gameService.epsilon,
		                                             gameService.learningSamplesLimit,
		                                             gameService.botsDirectory,
		                                             gameService.samplesDirectory,
		                                             gameService.allowBotDuplication,
		                                             gameService.botBindings)
	}

	/*
	GameService saves data in 3 forms:
	- GameService.Params - one file - contains learning parameters that need to be persisted
	- Bots data - each bot in one file - updated after learning; bots can be reloaded by using endpoint
	- Learning samples - each epoch in one file - GameService does not alter previously saved files but only incrementally
	  saves new samples; samples can be reloaded by using endpoint
	 */

	private val coroutineJob = Job()
	private val coroutineScope = CoroutineScope(coroutineJob)

	private val logger: Logger = LoggerFactory.getLogger(javaClass)
	private val dataSerializer = DataSerializer()
	private val initialParams = dataSerializer.loadGameServiceParams()

	var actionThreshold = initialParams?.actionThreshold ?: 0f
		set(value)
		{
			field = value
			saveParams()
		}
	var learnRate = initialParams?.learnRate ?: 0.0001f
		set(value)
		{
			field = value
			saveParams()
		}
	var discountFactor = initialParams?.discountFactor ?: 0.98f
		set(value)
		{
			field = value
			saveParams()
		}
	var epsilon = initialParams?.epsilon ?: 0.1f
		set(value)
		{
			field = value
			saveParams()
		}
	var learningSamplesLimit = initialParams?.learningSamplesLimit ?: 50000
		set(value)
		{
			field = value
			saveParams()
			ensureLearningSamplesAmount()
		}
	var botsDirectory = initialParams?.botsDirectory ?: "default"
		set(value)
		{
			field = value
			saveParams()
			reloadBotsData()
		}
	var samplesDirectory = initialParams?.samplesDirectory ?: "default"
		set(value)
		{
			field = value
			saveParams()
			reloadSamples()
		}
	var allowBotDuplication = initialParams?.allowBotDuplication ?: false
		set(value)
		{
			field = value
			saveParams()
		}
	var botBindings = initialParams?.botBindings ?: emptyMap()
		set(value)
		{
			field = value
			saveParams()
		}

	private val gameManager = StandardGameManager(coroutineScope) { side -> createBotFromRandomData(side) }
	private var botsData = dataSerializer.loadBots(botsDirectory)
	private var learningSamples = dataSerializer.loadSamples(samplesDirectory, learningSamplesLimit)// Newest samples in the end
	private var busyBots = emptyList<String>()

	val botsNames get() = botsData.map { it.key }
	val samplesAmount get() = learningSamples.size

	@PostConstruct
	fun onStart()
	{
		logger.debug("GameService created")
		gameManager.setGameListener(this)
	}

	private fun createBotFromRandomData(side: Player.Side): DarvinBotWithDQNAgent
	{
		fun getBoundBotData() =
				botBindings[side]?.let { name -> name to (botsData[name] ?: throw Exception("Unknown bot data: $name")) }
		fun getRandomBotData() =
				botsData.filterKeys { allowBotDuplication || it !in busyBots }.entries.randomOrNull()?.toPair()

		val (name, botData) = getBoundBotData() ?: getRandomBotData() ?: throw Exception("No bots")
		logger.info("Using bot $name for $side.")
		busyBots = busyBots + name
		return createBot(side, botData)
	}

	private fun createBot(side: Player.Side, data: DarvinBotData) =
			DarvinBot(createAgent(side, data.agentData), StandardEnvironment(side))

	private fun createAgent(side: Player.Side, data: DQNAgentData) =
			DQNAgent(side, actionThreshold, epsilon, createNetwork(data.networkData))

	private fun createNetwork(data: AxonDQNetworkData) = AxonDQNetwork(data)

	fun updateStateAndGetOrder(gameState: GameState.GameStateData): Order
	{
		val startTime = System.currentTimeMillis()
		return gameManager.updateStateAndGetOrder(gameState).also {
			val elapsedTime = System.currentTimeMillis() - startTime
			logger.debug("Calculated response in $elapsedTime ms")
		}
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
		busyBots = emptyList()
	}

	override fun onGameEnd(bots: List<DarvinBotWithDQNAgent>)
	{
		logger.info("Game ended")
		busyBots = emptyList()
		bots.forEach { bot -> bot.calculateAndSaveSamples() }
	}

	private fun DarvinBotWithDQNAgent.calculateAndSaveSamples()
	{
		val samples = agent.calculateLearningSamples(discountFactor)
		saveSamples(samples)

		learningSamples = learningSamples + samples
		ensureLearningSamplesAmount()
	}

	private fun ensureLearningSamplesAmount()
	{
		val exceedingSamples = learningSamples.size - learningSamplesLimit
		if(exceedingSamples > 0) learningSamples = learningSamples.drop(exceedingSamples)
	}

	private fun reloadSamples()
	{
		learningSamples = dataSerializer.loadSamples(samplesDirectory, learningSamplesLimit)
	}

	private fun reloadBotsData()
	{
		botsData = dataSerializer.loadBots(botsDirectory)
	}

	private fun saveParams()
	{
		dataSerializer.saveGameServiceData(Params(this))
	}

	private fun saveBotData(botData: DarvinBotData, name: String)
	{
		val elapsedTime = measureTimeMillis {
			dataSerializer.saveBot(botData, botsDirectory, name)
		}
		logger.debug("Saved bot data in $elapsedTime ms")
	}

	private fun saveSamples(samples: List<DQNAgent.LearningSample>)
	{
		fun createSamplesFilename() = SimpleDateFormat("yyyy-MM-dd HH-mm-ss").format(Date())

		val elapsedTime = measureTimeMillis {
			dataSerializer.saveSamples(samples, samplesDirectory, createSamplesFilename())
		}
		logger.debug("Saved samples in $elapsedTime ms")
	}

	// API FUNCTIONS

	//Creates bot with network with weights randomly distributed between -randomRange to randomRange
	fun addNewBot(name: String, randomRange: Float): Boolean
	{
		if(botsData.containsKey(name)) return false
		val network = AxonDQNetwork(-randomRange..randomRange)
		val botData = DarvinBotData(DQNAgentData(network.data))
		botsData = botsData + (name to botData)
		saveBotData(botData, name)
		return true
	}

	fun teach(samplesAmount: Int)
	{
		fun DQNetwork<*>.teach(samplesAmount: Int)
		{
			learningSamples.shuffled().take(samplesAmount).forEach { sample ->
				learn(sample.evaluation, sample.allErrors, learnRate)
			}
		}

		botsData = botsData.mapValues { (name, botData) ->
			val agentData = botData.agentData
			val networkData = agentData.networkData
			val network = AxonDQNetwork(networkData)
			network.teach(samplesAmount)
			botData.copy(agentData = agentData.copy(networkData = network.data)).also { saveBotData(it, name) }
		}
	}

	@PreDestroy
	fun onDestroy()
	{
		coroutineJob.cancel()
	}
}