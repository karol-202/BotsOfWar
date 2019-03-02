package pl.karol202.bow.game

import pl.karol202.bow.model.GameMap
import pl.karol202.bow.model.GameState

class Game private constructor(val gameMap: GameMap,
                               gameStateData: GameState.GameStateData)
{
	companion object
	{
		fun create(gameStateData: GameState.GameStateData): Game
		{
			val gameMap = GameMap.fromServer(gameStateData.mapPath!!)
			return Game(gameMap, gameStateData)
		}
	}

	private var gameState: GameState = gameStateData.toGameState(this)

	val width = gameMap.widthInTiles
	val height = gameMap.heightInTiles

	val player1 get() = gameState.player1
	val player2 get() = gameState.player2
	val players get() = listOf(player1, player2)
	val activePlayer get() = players.single { it.active }.side

	val entities get() = players.flatMap { it.entities }

	fun updateGameState(gameStateData: GameState.GameStateData)
	{
		gameState = gameStateData.toGameState(this)
	}
}