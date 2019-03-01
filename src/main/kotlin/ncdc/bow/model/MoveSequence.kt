package ncdc.bow.model

import org.newdawn.slick.util.pathfinding.Path

enum class Direction(val symbol: String)
{
	LEFT("L"),
	UP("U"),
	RIGHT("R"),
	DOWN("D");

	companion object
	{
		fun fromDeltaPosition(deltaX: Int, deltaY: Int) = when
		{
			deltaX == -1 && deltaY == 0 -> LEFT
			deltaX == 0 && deltaY == -1 -> UP
			deltaX == 1 && deltaY == 0 -> RIGHT
			deltaX == 0 && deltaY == 1 -> DOWN
			else -> throw IllegalArgumentException("Unknown direction: $deltaX, $deltaY")
		}
	}

	fun invertVertically() = when(this)
	{
		LEFT -> LEFT
		UP -> DOWN
		RIGHT -> RIGHT
		DOWN -> UP
	}
}

class LocalMoveSequence(val steps: List<Direction>)
{
	companion object
	{
		fun fromPath(path: Path): LocalMoveSequence
		{
			if(path.length <= 1) return LocalMoveSequence(emptyList())
			var lastX = path.getX(0)
			var lastY = path.getY(0)
			val steps = (1 until path.length).map { path.getStep(it) }.map { step ->
				val direction = Direction.fromDeltaPosition(step.x - lastX, step.y - lastY)
				lastX = step.x
				lastY = step.y
				direction
			}
			return LocalMoveSequence(steps)
		}
	}

	fun toServerSystem() = ServerMoveSequence(steps.map { it.invertVertically() })
}

class ServerMoveSequence(val steps: List<Direction>)
