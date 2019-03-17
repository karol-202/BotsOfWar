package pl.karol202.bow.game

import pl.karol202.bow.model.*

class Game private constructor(val map: GameMap,
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

	val width = map.width
	val height = map.height

	val settings = GameSettings.fromServer(this)
	val entitySettings = settings.entitySettings.values.toList()

	var state = gameStateData.toGameState(this)
		private set

	val id get() = state.gameId

	fun updateState(gameStateData: GameState.GameStateData)
	{
		state = gameStateData.toGameState(this)
	}

	fun isPositionWalkable(position: LocalPosition) = map[position]?.walkable == true

	fun checkWinner() = GameResult.checkWinner(this)
}