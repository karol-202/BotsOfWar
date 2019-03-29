package pl.karol202.bow.robot

import com.fasterxml.jackson.databind.ObjectMapper
import pl.karol202.bow.game.DarvinGameManager
import java.io.File

class DataSerializer(private val file: File)
{
	private val jacksonMapper = ObjectMapper()

	fun loadData() = file.takeIf { it.exists() }
			?.let { jacksonMapper.readValue(it, DarvinGameManager.Data::class.java) }

	fun saveData(data: DarvinGameManager.Data)
	{
		jacksonMapper.writerWithDefaultPrettyPrinter().writeValue(file, data)
	}
}