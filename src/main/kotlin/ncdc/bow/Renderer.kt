package ncdc.bow

import org.newdawn.slick.util.pathfinding.Path

interface Renderer
{
	fun render(map: Map, path: Path?, coordinates: List<LocalPosition>?, entities: List<Entity>?)
}