package pl.karol202.bow.service

import com.google.gson.Gson
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

	private data class NamedSamples(val name: String,
	                                val samples: List<DQNAgent.LearningSample>)

	private val robotParamsFile = File(ROBOT_PARAMS_FILE_PATH)
	private val robotDataFile = File(ROBOT_DATA_FILE_PATH)
	private val gameServiceDataFile = File(GAME_SERVICE_DATA_FILE_PATH)
	private val botsDir = File(BOTS_DIR_PATH)
	private val samplesDir = File(SAMPLES_DIR_PATH)

	private val gson = Gson()

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
						.associate { file -> file.name to loadBotFromFile(file) }
	}

	fun saveBot(botData: DarvinBotData, directoryName: String, filename: String) =
			saveToFile(File(getBotsDirectory(directoryName), filename), BotDataWrapper(botData))

	private fun getBotsDirectory(directoryName: String) = File(botsDir, directoryName)

	//Load 'limit' newest samples for each bot and return them in the map of lists where the newest samples are on the end
	fun loadSamples(directoryName: String, limit: Int?) = synchronized(samplesDir) {
		fun loadSamplesFromFile(file: File): NamedSamples =
				file.reader().use { reader -> gson.fromJson(reader, NamedSamples::class.java) }

		val samplesListsMap = mutableMapOf<String, MutableList<DQNAgent.LearningSample>>()

		fun addFromFile(file: File)
		{
			val namedSamples = loadSamplesFromFile(file)
			val samplesList = samplesListsMap.getOrPut(namedSamples.name) { mutableListOf() }
			namedSamples.samples.forEach { sample ->
				if(limit != null && samplesList.size >= limit) return@forEach
				samplesList.add(sample)
			}
		}

		val directory = getSamplesDirectory(directoryName).apply { if(!exists()) mkdirs() }
		directory.listFiles { _, name -> !name.startsWith("_") }
				.sortedByDescending { it.name } // Newest samples first
				.forEach { file -> addFromFile(file) }

		samplesListsMap.mapValues { it.value.reversed() } // For newest samples to be on the end
	}

	fun saveSamples(samples: List<DQNAgent.LearningSample>, directoryName: String, botName: String) =
			saveToFile(File(getSamplesDirectory(directoryName), createSamplesFilename(botName)), NamedSamples(botName, samples))

	private fun createSamplesFilename(botName: String) =
			"${SimpleDateFormat("yyyy-MM-dd HH-mm-ss").format(Date())} $botName"

	private fun getSamplesDirectory(directoryName: String) = File(samplesDir, directoryName)

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