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
import pl.karol202.bow.model.Player
import pl.karol202.bow.util.DataSerializer
import pl.karol202.bow.util.randomOrNull
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

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

	data class Params(val actionThreshold: Float = 0f,
	                  val discountFactor: Float = 0.98f,
	                  val epsilon: Float = 0.1f,
	                  val learningSamplesLimit: Int = 5000,
	                  val botsDirectory: String = "default",
	                  val samplesDirectory: String = "default",
	                  val allowBotDuplication: Boolean = false,
	                  val botBindings: Map<Player.Side, String> = emptyMap())

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
	private val gameManager = StandardGameManager(coroutineScope) { side -> createBotFromRandomData(side) }

	var params = dataSerializer.loadGameServiceParams() ?: Params()
		set(value)
		{
			val oldParams = field
			field = value
			saveParams()
			if(learningSamplesLimit != oldParams.learningSamplesLimit) ensureLearningSamplesAmount()
			if(botsDirectory != oldParams.botsDirectory) reloadBotsData()
			if(samplesDirectory != oldParams.samplesDirectory) reloadSamples()
		}
	private var botsData = dataSerializer.loadBots(botsDirectory)
	private var learningSamples = dataSerializer.loadSamples(samplesDirectory, learningSamplesLimit) // Newest samples in the end
	private var busyBots = emptyMap<String, DarvinBotWithDQNAgent>()

	private val actionThreshold get() = params.actionThreshold
	private val discountFactor get() = params.discountFactor
	private val epsilon get() = params.epsilon
	private val learningSamplesLimit get() = params.learningSamplesLimit
	private val botsDirectory get() = params.botsDirectory
	private val samplesDirectory get() = params.samplesDirectory
	private val allowBotDuplication get() = params.allowBotDuplication
	private val botBindings get() = params.botBindings

	val botsNames get() = botsData.map { it.key }
	val samplesAmount get() = learningSamples.mapValues { (_, list) -> list.size }

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
		val bot = createBot(side, botData)
		logger.info("Using bot $name for $side.")
		busyBots = busyBots + (name to bot)
		return bot
	}

	private fun createBot(side: Player.Side, data: DarvinBotData) =
			DarvinBot(createAgent(side, data.agentData), StandardEnvironment(side))

	private fun createAgent(side: Player.Side, data: DQNAgentData) =
			DQNAgent(side, actionThreshold, epsilon, createNetwork(data.networkData))

	private fun createNetwork(data: AxonDQNetworkData) = AxonDQNetwork(data)

	fun updateStateAndGetOrder(gameState: GameState.GameStateData) =
			measureTimeAndLog("Calculated response") { gameManager.updateStateAndGetOrder(gameState) }

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
		busyBots = emptyMap()
	}

	override fun onGameEnd(bots: List<DarvinBotWithDQNAgent>)
	{
		logger.info("Game ended")
		bots.forEach { bot -> bot.calculateAndSaveSamples() }
		busyBots = emptyMap()
	}

	private fun DarvinBotWithDQNAgent.calculateAndSaveSamples()
	{
		val name = busyBots.entries.singleOrNull { it.value === this }?.key ?: throw IllegalStateException("Unknown bot")
		val samples = agent.calculateLearningSamples(discountFactor)
		logger.debug("Calculated samples for $name")
		saveSamples(samples, name)

		val oldSamples = learningSamples[name] ?: emptyList()
		val mixedSamples = oldSamples + samples
		learningSamples = learningSamples + (name to mixedSamples)
		ensureLearningSamplesAmount()
	}

	private fun ensureLearningSamplesAmount()
	{
		learningSamples = learningSamples.mapValues { (_, samples) ->
			val exceedingSamples = learningSamples.size - learningSamplesLimit
			if(exceedingSamples > 0) samples.drop(exceedingSamples) else samples
		}
	}

	private fun saveSamples(samples: List<DQNAgent.LearningSample>, botName: String) =
			measureTimeAndLog("Saved samples") {
				dataSerializer.saveSamples(samples, samplesDirectory, botName)
			}

	private fun saveBotData(botData: DarvinBotData, name: String) = measureTimeAndLog("Saved bot data") {
		dataSerializer.saveBot(botData, botsDirectory, name)
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
		dataSerializer.saveGameServiceData(params)
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

	fun teach(learnRate: Float, samplesAmount: Int, botsNames: List<String>?)
	{
		fun DQNetwork<*>.teach(botName: String, samplesAmount: Int) = measureTimeAndLog("Taught network") {
			val samples = learningSamples[botName] ?: emptyList()
			samples.shuffled().take(samplesAmount).forEach { sample ->
				learn(sample.evaluation, sample.allErrors, learnRate)
			}
		}

		botsData = botsData.mapValues { (name, botData) ->
			if(botsNames != null && name !in botsNames) return@mapValues botData
			val agentData = botData.agentData
			val networkData = agentData.networkData
			val network = AxonDQNetwork(networkData)
			network.teach(name, samplesAmount)
			botData.copy(agentData = agentData.copy(networkData = network.data)).also { saveBotData(it, name) }
		}
	}

	@PreDestroy
	fun onDestroy()
	{
		coroutineJob.cancel()
	}

	private fun <T> measureTimeAndLog(message: String, block: () -> T): T
	{
		val startTime = System.currentTimeMillis()
		return block().also { logger.debug("$message in ${System.currentTimeMillis() - startTime} ms") }
	}
}