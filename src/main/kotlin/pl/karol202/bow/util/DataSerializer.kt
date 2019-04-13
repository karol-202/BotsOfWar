package pl.karol202.bow.util

import com.google.gson.Gson
import pl.karol202.bow.darvin.agent.DQNAgent
import pl.karol202.bow.service.DarvinBotData
import pl.karol202.bow.service.GameService
import java.io.File

class DataSerializer
{
	companion object
	{
		private const val PARAMS_FILE_PATH = "data/params.dat"
		private const val BOTS_DIR_PATH = "data/bots/"
		private const val SAMPLES_DIR_PATH = "data/samples/"
	}

	private data class BotDataWrapper(val botData: DarvinBotData)

	private data class SamplesList(val samples: List<DQNAgent.LearningSample>)

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

	//Load 'limit' newest samples and return them in list where the newest samples are on the end
	fun loadSamples(directoryName: String, limit: Int?): List<DQNAgent.LearningSample>
	{
		fun loadSamplesFromFile(file: File) =
				file.reader().use { reader -> gson.fromJson(reader, SamplesList::class.java)?.samples }

		// Returns false if list is full, true otherwise
		fun MutableList<DQNAgent.LearningSample>.addFromFile(file: File): Boolean
		{
			loadSamplesFromFile(file)?.forEach { sample ->
				add(sample)
				if(limit != null && size >= limit) return false
			}
			return true
		}

		val samples = mutableListOf<DQNAgent.LearningSample>()
		val directory = getSamplesDirectory(directoryName).apply { if(!exists()) mkdirs() }
		directory.listFiles { _, name -> !name.startsWith("_") }
				.sortedByDescending { it.name }
				.forEach { file -> if(!samples.addFromFile(file)) return@forEach }
		return samples.reversed()
	}

	fun saveSamples(samples: List<DQNAgent.LearningSample>, directoryName: String, filename: String) =
			File(getSamplesDirectory(directoryName), filename).writer().use { writer ->
				gson.toJson(SamplesList(samples), writer)
			}

	private fun getSamplesDirectory(directoryName: String) = File(samplesDir, directoryName)
}