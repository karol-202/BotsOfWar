package ncdc.bow

import kotlinx.html.*
import kotlinx.html.stream.appendHTML
import org.newdawn.slick.util.pathfinding.AStarPathFinder
import org.newdawn.slick.util.pathfinding.Path
import java.io.File
import java.io.Writer

fun start()
{
	val map = Map.fromServer()
	map.generateHTMLFile("out/html/map.html")

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

private fun Map.generateHTMLFile(path: String)
{
	val file = File(path)
	file.createNewFile()
	generateHTML(file.bufferedWriter())
}

private fun Map.generateHTML(writer: Writer)
{
	writer.appendHTML().html {
		head {
			title { + "Mapa" }
		}
		body {
			table {
				data.forEach { row ->
					tr {
						row.forEach { cell ->
							td {
								style = "padding: 0"
								img(cell.name, cell.getImage()) {
									width = "48px"
									height = "48px"
								}
							}
						}
					}
				}
			}
		}
	}.flush()
}

private fun Map.Cell.getImage() = when(this)
{
	Map.Cell.DIRT -> "../../src/main/resources/tiles/dirt.png"
	Map.Cell.GRASS -> "../../src/main/resources/tiles/grass.png"
	Map.Cell.WATER -> "../../src/main/resources/tiles/water.png"
	Map.Cell.ROCK -> "../../src/main/resources/tiles/rock.png"
	Map.Cell.BASE -> "../../src/main/resources/tiles/base.png"
	Map.Cell.MINE -> "../../src/main/resources/tiles/mine.png"
}
