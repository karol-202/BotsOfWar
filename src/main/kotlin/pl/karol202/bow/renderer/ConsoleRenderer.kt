package pl.karol202.bow.renderer

import pl.karol202.bow.game.Game
import pl.karol202.bow.model.GameMap
import pl.karol202.bow.model.LocalPosition

class ConsoleRenderer : Renderer
{
	override fun render(game: Game)
	{
		game.gameMap.data.forEachIndexed { y, column ->
			column.forEachIndexed { x, cell ->
				renderCell(game, LocalPosition(x, y), cell)
			}
			println()
		}
	}

	private fun renderCell(game: Game, position: LocalPosition, cell: GameMap.Cell) = when
	{
		game.player1.entities.any { it.position == position } -> print("6 ")
		game.player2.entities.any { it.position == position } -> print("7 ")
		else -> print("${cell.ordinal} ")
	}
}