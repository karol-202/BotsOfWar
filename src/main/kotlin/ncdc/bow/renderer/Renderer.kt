package ncdc.bow.renderer

import ncdc.bow.model.Entity
import ncdc.bow.model.GameMap
import ncdc.bow.model.LocalPosition
import org.newdawn.slick.util.pathfinding.Path

interface Renderer
{
	fun render(gameMap: GameMap, path: Path?, coordinates: List<LocalPosition>?, entities: List<Entity>?)
}