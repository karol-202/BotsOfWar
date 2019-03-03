package pl.karol202.bow.model

import pl.karol202.bow.game.Game

//Server system: (0, 0) in left bottom corner
data class ServerPosition(val x: Int = 0,
                          val y: Int = 0)
{
	fun toLocalSystem(game: Game) = LocalPosition(x, game.height - y - 1)
}

//Local system: (0, 0) in left top corner
data class LocalPosition(val x: Int = 0,
                         val y: Int = 0)
{
	operator fun plus(other: LocalPosition) = LocalPosition(x + other.x, y + other.y)

	operator fun minus(other: LocalPosition) = LocalPosition(x - other.x, y - other.y)

	operator fun times(factor: Int) = LocalPosition(x * factor, y * factor)
}
