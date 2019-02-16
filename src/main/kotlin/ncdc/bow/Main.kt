package ncdc.bow

import org.newdawn.slick.util.pathfinding.AStarPathFinder
import org.newdawn.slick.util.pathfinding.Path

fun start()
{
	val map = Map.fromServer()

	val finder = AStarPathFinder(map, 1000, false)
	val path: Path? = finder.findPath(null, 0, 0, 10, 7)
	if(path != null)
	{
		map.printWithPath(path)
		val moves = MoveSequence.fromPath(path)
		moves.print()
	}
	else println("Path not found")
}

private fun Map.print()
{
	data.forEach { column ->
		column.forEach { cell ->
			print("${cell.ordinal} ")
		}
		println()
	}
}

private fun Path.print()
{
	(0 until length).map { getStep(it) }.forEach {
		println("Step: ${it.x}, ${it.y}")
	}
}

private fun Map.printWithPath(path: Path)
{
	data.forEachIndexed { y, column ->
		column.forEachIndexed { x, cell ->
			if(path.contains(x, y)) print("# ")
			else print("${cell.ordinal} ")
		}
		println()
	}
}

private fun MoveSequence.print()
{
	moves.forEach { println(it.name) }
}
