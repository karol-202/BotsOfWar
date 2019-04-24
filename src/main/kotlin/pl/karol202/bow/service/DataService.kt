package pl.karol202.bow.service

import com.google.gson.Gson
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import pl.karol202.bow.darvin.agent.DQNAgent
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Service
class DataService
{
	companion object
	{
		private const val ROBOT_PARAMS_FILE_PATH = "data/robot_params"
		private const val ROBOT_DATA_FILE_PATH = "data/robot_data"
		private const val GAME_SERVICE_DATA_FILE_PATH = "data/game_service_params"
		private const val BOTS_DIR_PATH = "data/bots/"
		private const val SAMPLES_DIR_PATH = "data/samples/"
	}

	private data class BotDataWrapper(val botData: DarvinBotData)

	private data class SamplesWrapper(val samples: List<DQNAgent.LearningSample>)

	private val logger: Logger = LoggerFactory.getLogger(javaClass)
	private val gson = Gson()

	private val robotParamsFile = File(ROBOT_PARAMS_FILE_PATH)
	private val robotDataFile = File(ROBOT_DATA_FILE_PATH)
	private val gameServiceDataFile = File(GAME_SERVICE_DATA_FILE_PATH)
	private val botsDir = File(BOTS_DIR_PATH)
	private val samplesDir = File(SAMPLES_DIR_PATH)

	fun loadRobotParams() = loadFromFile<RobotService.Params>(robotParamsFile)

	fun saveRobotParams(params: RobotService.Params) = saveToFile(robotParamsFile, params)

	fun loadRobotData() = loadFromFile<RobotService.Data>(robotDataFile)

	fun saveRobotData(data: RobotService.Data) = saveToFile(robotDataFile, data)

	fun loadGameServiceParams() = loadFromFile<GameService.Params>(gameServiceDataFile)

	fun saveGameServiceParams(params: GameService.Params) = saveToFile(gameServiceDataFile, params)

	fun loadBots(directoryName: String): Map<String, DarvinBotData> = synchronized(botsDir) {
		fun loadBotFromFile(file: File) =
				file.reader().use { reader -> gson.fromJson<BotDataWrapper>(reader, BotDataWrapper::class.java) }.botData

		val directory = getBotsDirectory(directoryName).also { if(!it.exists()) it.mkdirs() }

		directory.listFiles { _, name -> !name.startsWith("_") }
						.associate { file ->
							logger.debug("Loading bot: ${file.name}")
							file.name to loadBotFromFile(file)
						}
	}

	fun saveBot(botData: DarvinBotData, directoryName: String, botName: String) =
			saveToFile(getBotFile(directoryName, botName), BotDataWrapper(botData))

	private fun getBotFile(directoryName: String, botName: String) = File(getBotsDirectory(directoryName), botName)

	private fun getBotsDirectory(directoryName: String) = File(botsDir, directoryName)

	//Load 'limit' newest samples for each bot and return them in the map of lists where the newest samples are on the end
	fun loadSamples(directoryName: String, limit: Int?): Map<String, List<DQNAgent.LearningSample>> = synchronized(samplesDir) {
		val samplesListsMap = mutableMapOf<String, MutableList<DQNAgent.LearningSample>>()

		fun loadFromBotDirectory(botDirectory: File)
		{
			val botName = botDirectory.name

			fun loadFromFile(file: File): Boolean // If true: load further samples
			{
				logger.debug("Loading samples for $botName from ${file.name}")

				val targetList = samplesListsMap.getOrPut(botName) { mutableListOf() }
				file.reader().use { reader -> gson.fromJson(reader, SamplesWrapper::class.java) }.samples.forEach { sample ->
					if(limit != null && targetList.size >= limit) return false
					targetList.add(sample)
				}
				return true
			}

			botDirectory.listFiles { file -> file.isFile && !file.name.startsWith("_") }
					.sortedByDescending { it.name } // Newest samples first
					.forEach { file -> if(!loadFromFile(file)) return@forEach } // Ignore rest if reached limit
		}

		val mainDirectory = getSamplesDirectory(directoryName)
		mainDirectory.listFiles { file -> file.isDirectory }
				.forEach { file -> loadFromBotDirectory(file) }

		samplesListsMap.mapValues { it.value.reversed() } // For newest samples to be on the end
	}

	fun saveSamples(samples: List<DQNAgent.LearningSample>, directoryName: String, botName: String) =
			saveToFile(getSamplesFile(directoryName, botName), SamplesWrapper(samples))

	private fun getSamplesFile(directoryName: String, botName: String) =
			File(getSamplesDirectoryForBot(directoryName, botName), createSamplesFilename(botName))

	private fun getSamplesDirectoryForBot(directoryName: String, botName: String) =
			File(getSamplesDirectory(directoryName), botName).apply { if(!exists()) mkdirs() }

	private fun getSamplesDirectory(directoryName: String) =
			File(samplesDir, directoryName).apply { if(!exists()) mkdirs() }

	private fun createSamplesFilename(botName: String) =
			"${SimpleDateFormat("yyyy-MM-dd HH-mm-ss").format(Date())} $botName"

	private inline fun <reified T> loadFromFile(file: File) = synchronized(file) {
		file.takeIf { it.exists() }?.reader()?.use { reader ->
			gson.fromJson(reader, T::class.java)
		}
	}

	private fun saveToFile(file: File, data: Any) = synchronized(file) {
		file.writer().use { writer ->
			gson.toJson(data, writer)
		}
	}
}