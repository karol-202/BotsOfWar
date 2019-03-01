package ncdc.bow.model

import ncdc.bow.APIException
import org.newdawn.slick.util.pathfinding.PathFindingContext
import org.newdawn.slick.util.pathfinding.TileBasedMap
import org.springframework.web.client.RestTemplate

//Data is list of rows, that are lists of cells. First row is upper edge and last is bottom.
class GameMap private constructor(val data: List<List<Cell>>) : TileBasedMap
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
		fun fromServer(endpoint: String): GameMap
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

	operator fun get(x: Int, y: Int) = data[y][x]

	override fun blocked(context: PathFindingContext?, tx: Int, ty: Int) = !this[tx, ty].walkable

	override fun getCost(context: PathFindingContext?, tx: Int, ty: Int) = 1f

	override fun pathFinderVisited(x: Int, y: Int) {}

	override fun getWidthInTiles() = data[0].size

	override fun getHeightInTiles() = data.size
}