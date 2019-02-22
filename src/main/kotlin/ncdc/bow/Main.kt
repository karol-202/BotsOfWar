package ncdc.bow

import org.newdawn.slick.util.pathfinding.AStarPathFinder
import org.newdawn.slick.util.pathfinding.Path

fun start()
{
	val world = World.fromServer()

	val finder = AStarPathFinder(world.map, 1000, true)
	val path: Path? = finder.findPath(null, 0, 0, 10, 7)
	if(path != null)
	{
		val moves = MoveSequence.fromPath(path)
		println("Path steps:")
		moves.steps.forEach { println(it.name) }
	}
	else println("Path not found")

	val coordinates = Coordinates.fromServer(world)

	val renderer = HTMLRenderer("out/html/map.html")
	//val renderer = ConsoleRenderer()
	renderer.render(world.map, path, null, world.entities)
}
