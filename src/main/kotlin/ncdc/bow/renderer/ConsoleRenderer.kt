package ncdc.bow.renderer

import ncdc.bow.model.Entity
import ncdc.bow.model.GameMap
import ncdc.bow.model.LocalPosition
import org.newdawn.slick.util.pathfinding.Path

class ConsoleRenderer : Renderer
{
	override fun render(gameMap: GameMap, path: Path?, coordinates: List<LocalPosition>?, entities: List<Entity>?)
	{
		gameMap.data.forEachIndexed { y, column ->
			column.forEachIndexed { x, cell ->
				val position = LocalPosition(x, y)

				when
				{
					path?.contains(x, y) == true -> print("# ")
					coordinates?.contains(position) == true -> print("6 ")
					entities?.any { it.position == position } == true -> print("7 ")
					else -> print("${cell.ordinal} ")
				}
			}
			println()
		}
	}
}