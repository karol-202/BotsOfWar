package ncdc.bow.model

class World private constructor(val gameMap: GameMap)
{
	companion object
	{
		fun fromServer() = World(GameMap.fromServer()).apply {
			updateEntities()
		}
	}

	var entities = emptyList<Entity>()
		private set

	val width = gameMap.widthInTiles
	val height = gameMap.heightInTiles

	private fun updateEntities()
	{
		entities = Entity.allFromServer(this)
	}
}