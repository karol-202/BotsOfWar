package pl.karol202.bow.robot

import com.google.gson.Gson
import pl.karol202.bow.game.DarvinGameManager
import java.io.File

class DataSerializer(private val file: File)
{
	private val gson = Gson()

	fun loadData() = file.takeIf { it.exists() }?.reader()?.use { reader ->
		gson.fromJson(reader, DarvinGameManager.Data::class.java)
	}

	fun saveData(data: DarvinGameManager.Data)
	{
		file.writer().use { writer -> gson.toJson(data, writer) }
	}
}