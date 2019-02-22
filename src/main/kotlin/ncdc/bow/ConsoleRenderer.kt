package ncdc.bow

import org.newdawn.slick.util.pathfinding.Path

class ConsoleRenderer : Renderer
{
	override fun render(map: Map, path: Path?, coordinates: List<LocalPosition>?, entities: List<Entity>?)
	{
		map.data.forEachIndexed { y, column ->
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