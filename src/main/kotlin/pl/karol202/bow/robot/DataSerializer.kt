package pl.karol202.bow.robot

import com.fasterxml.jackson.databind.ObjectMapper
import pl.karol202.bow.bot.neural.DarvinReinforcementNetwork
import java.io.File

class DataSerializer(private val file: File)
{
	private data class DataModel(val layers: List<List<FloatArray>> = emptyList())

	private val jacksonMapper = ObjectMapper()

	fun loadData() = file.takeIf { it.exists() }
			?.let { jacksonMapper.readValue(it, DataModel::class.java) }
			?.let { DarvinReinforcementNetwork.Data(it.layers) }

	fun saveData(data: DarvinReinforcementNetwork.Data)
	{
		jacksonMapper.writerWithDefaultPrettyPrinter().writeValue(file, data)
	}
}