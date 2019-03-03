package pl.karol202.bow.model

import org.newdawn.slick.util.pathfinding.Path

enum class Direction(val symbol: String,
                     val offset: LocalPosition)
{
	LEFT("L", LocalPosition(-1, 0)),
	UP("U", LocalPosition(0, -1)),
	RIGHT("R", LocalPosition(1, 0)),
	DOWN("D", LocalPosition(0, 1));

	companion object
	{
		fun fromOffset(offset: LocalPosition) = values().single { it.offset == offset }
	}

	fun invertVertically() = when(this)
	{
		LEFT -> LEFT
		UP -> DOWN
		RIGHT -> RIGHT
		DOWN -> UP
	}
}

class MoveSequence(val steps: List<Direction>)
{
	companion object
	{
		fun fromPath(path: Path): MoveSequence
		{
			if(path.length <= 1) return MoveSequence(emptyList())
			var lastPosition = LocalPosition(path.getX(0), path.getY(0))
			val steps = (1 until path.length).map { path.getStep(it) }.map { step ->
				val currentPosition = LocalPosition(step.x, step.y)
				val direction = Direction.fromOffset(currentPosition - lastPosition)
				lastPosition = currentPosition
				direction
			}
			return MoveSequence(steps)
		}
	}
}
