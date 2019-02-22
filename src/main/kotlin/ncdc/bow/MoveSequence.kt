package ncdc.bow

import org.newdawn.slick.util.pathfinding.Path

class MoveSequence private constructor(val steps: List<Direction>)
{
	companion object
	{
		fun fromPath(path: Path): MoveSequence
		{
			if(path.length <= 1) return MoveSequence(emptyList())
			var lastX = path.getX(0)
			var lastY = path.getY(0)
			val steps = (1 until path.length).map { path.getStep(it) }.map { step ->
				val direction = Direction.fromDeltaPosition(step.x - lastX, step.y - lastY)
				lastX = step.x
				lastY = step.y
				direction
			}
			return MoveSequence(steps)
		}
	}

	enum class Direction
	{
		LEFT, TOP, RIGHT, BOTTOM;

		companion object
		{
			fun fromDeltaPosition(deltaX: Int, deltaY: Int) = when
			{
				deltaX == -1 && deltaY == 0 -> LEFT
				deltaX == 0 && deltaY == -1 -> TOP
				deltaX == 1 && deltaY == 0 -> RIGHT
				deltaX == 0 && deltaY == 1 -> BOTTOM
				else -> throw IllegalArgumentException("Unknown direction: $deltaX, $deltaY")
			}
		}
	}
}