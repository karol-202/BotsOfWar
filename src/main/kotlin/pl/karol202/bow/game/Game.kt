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

	val width = gameMap.width
	val height = gameMap.height

	val gameSettings = GameSettings.fromServer(this)
	val entitySettings get() = gameSettings.entitySettings.values.toList()

	var state: GameState = gameStateData.toGameState(this)
		private set

	val player1 get() = state.player1
	val player2 get() = state.player2
	val activePlayer get() = state.activePlayer
	val allEntities get() = state.allEntities

	fun updateGameState(gameStateData: GameState.GameStateData)
	{
		state = gameStateData.toGameState(this)
	}

	fun isPositionWalkable(position: LocalPosition) = gameMap[position]?.walkable == true
}