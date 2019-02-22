package ncdc.bow

class World private constructor(val map: Map)
{
	companion object
	{
		fun fromServer() = World(Map.fromServer()).apply {
			updateEntities()
		}
	}

	var entities = emptyList<Entity>()
		private set

	val width = map.widthInTiles
	val height = map.heightInTiles

	private fun updateEntities()
	{
		entities = Entity.allFromServer(this)
	}
}