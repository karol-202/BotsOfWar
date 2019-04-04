package pl.karol202.bow.model

import org.springframework.web.client.RestTemplate
import pl.karol202.bow.service.GameService
import pl.karol202.bow.util.APIException

//Data is list of rows, that are lists of cells. First row is upper edge and last is bottom.
class GameMap private constructor(val data: List<List<Cell>>)
{
	enum class Cell(val walkable: Boolean)
	{
		DIRT(true),
		GRASS(true),
		WATER(false),
		ROCK(false),
		BASE(false),
		MINE(false)
	}

	companion object
	{
		private val endpoint get() = "${GameService.serverAddress}/getMap"

		fun fromServer(): GameMap
		{
			val template = RestTemplate()
			val dataArray = template.getForObject(endpoint, Array<Array<Cell>>::class.java)
					?: throw APIException("Cannot fetch map.")
			if(!dataArray.ensureSize()) throw APIException("Invalid data")

			val dataList = dataArray.convertToList()
			return GameMap(dataList)
		}

		//Checks if the list is not empty and if all rows have equal size
		private fun Array<Array<Cell>>.ensureSize() = !this.isEmpty() && this.map { it.size }.distinct().size == 1

		private fun Array<Array<Cell>>.convertToList() = this.map { it.toList() }
	}

	val width = data[0].size
	val height = data.size

	operator fun get(x: Int, y: Int) = data.getOrNull(y)?.getOrNull(x)

	operator fun get(position: LocalPosition) = this[position.x, position.y]
}