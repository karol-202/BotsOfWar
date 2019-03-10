package pl.karol202.bow.model

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
}
