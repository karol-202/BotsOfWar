package pl.karol202.bow.util

import com.google.gson.Gson
import pl.karol202.bow.darvin.agent.DQNAgent
import pl.karol202.bow.service.DarvinBotData
import pl.karol202.bow.service.GameService
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class DataSerializer
{
	companion object
	{
		private const val PARAMS_FILE_PATH = "data/params.dat"
		private const val BOTS_DIR_PATH = "data/bots/"
		private const val SAMPLES_DIR_PATH = "data/samples/"
	}

	private data class BotDataWrapper(val botData: DarvinBotData)

	private data class NamedSamples(val name: String,
	                                val samples: List<DQNAgent.LearningSample>)

	private val paramsFile = File(PARAMS_FILE_PATH)
	private val botsDir = File(BOTS_DIR_PATH)
	private val samplesDir = File(SAMPLES_DIR_PATH)

	private val gson = Gson()

	fun loadGameServiceParams() = paramsFile.takeIf { it.exists() }?.reader()?.use { reader ->
		gson.fromJson(reader, GameService.Params::class.java)
	}

	fun saveGameServiceData(params: GameService.Params) = paramsFile.writer().use { writer ->
		gson.toJson(params, writer)
	}

	fun loadBots(directoryName: String): Map<String, DarvinBotData>
	{
		fun loadBotFromFile(file: File) =
				file.reader().use { reader -> gson.fromJson<BotDataWrapper>(reader, BotDataWrapper::class.java) }.botData

		val directory = getBotsDirectory(directoryName).also { if(!it.exists()) it.mkdirs() }
		return directory.listFiles { _, name -> !name.startsWith("_") }
						.associate { file -> file.name to loadBotFromFile(file) }
	}

	fun saveBot(botData: DarvinBotData, directoryName: String, filename: String) =
			File(getBotsDirectory(directoryName), filename).writer().use { writer ->
				gson.toJson(BotDataWrapper(botData), writer)
			}

	private fun getBotsDirectory(directoryName: String) = File(botsDir, directoryName)

	//Load 'limit' newest samples for each bot and return them in the map of lists where the newest samples are on the end
	fun loadSamples(directoryName: String, limit: Int?): Map<String, List<DQNAgent.LearningSample>>
	{
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

		return samplesListsMap.mapValues { it.value.reversed() } // For newest samples to be on the end
	}

	fun saveSamples(samples: List<DQNAgent.LearningSample>, directoryName: String, botName: String) =
			File(getSamplesDirectory(directoryName), createSamplesFilename(botName)).writer().use { writer ->
				gson.toJson(NamedSamples(botName, samples), writer)
			}

	private fun createSamplesFilename(botName: String) =
			"${SimpleDateFormat("yyyy-MM-dd HH-mm-ss").format(Date())} $botName"

	private fun getSamplesDirectory(directoryName: String) = File(samplesDir, directoryName)
}