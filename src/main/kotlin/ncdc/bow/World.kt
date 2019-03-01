package ncdc.bow

import ncdc.bow.model.GameMap
import ncdc.bow.model.GameState

class World private constructor(val gameMap: GameMap,
                                gameStateData: GameState.GameStateData)
{
	companion object
	{
		fun create(gameStateData: GameState.GameStateData): World
		{
			val gameMap = GameMap.fromServer(gameStateData.mapPath!!)
			return World(gameMap, gameStateData)
		}
	}

	var gameState: GameState = gameStateData.toGameState(this)

	val width = gameMap.widthInTiles
	val height = gameMap.heightInTiles
}