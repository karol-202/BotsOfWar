package pl.karol202.bow.game

import pl.karol202.bow.model.GameMap
import pl.karol202.bow.model.GameSettings
import pl.karol202.bow.model.GameState
import pl.karol202.bow.model.LocalPosition

class Game private constructor(val gameMap: GameMap,
                               gameStateData: GameState.GameStateData)
{
	companion object
	{
		fun create(gameStateData: GameState.GameStateData): Game
		{
			val gameMap = GameMap.fromServer()
			return Game(gameMap, gameStateData)
		}
	}

	val width = gameMap.widthInTiles
	val height = gameMap.heightInTiles

	val gameSettings = GameSettings.fromServer(this)
	val entitySettings get() = gameSettings.entitySettings.values.toList()

	var gameState: GameState = gameStateData.toGameState(this)
		private set

	val player1 get() = gameState.player1
	val player2 get() = gameState.player2
	val activePlayer get() = gameState.activePlayer
	val allEntities get() = gameState.allEntities

	fun updateGameState(gameStateData: GameState.GameStateData)
	{
		gameState = gameStateData.toGameState(this)
	}

	fun isPositionWalkable(position: LocalPosition) = gameMap[position]?.walkable == true
}