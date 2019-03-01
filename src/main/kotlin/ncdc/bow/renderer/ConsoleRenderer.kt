package ncdc.bow.renderer

import ncdc.bow.World
import ncdc.bow.model.GameMap
import ncdc.bow.model.LocalPosition

class ConsoleRenderer : Renderer
{
	override fun render(world: World)
	{
		world.gameMap.data.forEachIndexed { y, column ->
			column.forEachIndexed { x, cell ->
				renderCell(world, LocalPosition(x, y), cell)
			}
			println()
		}
	}

	private fun renderCell(world: World, position: LocalPosition, cell: GameMap.Cell) = when
	{
		world.gameState.player1.entities.any { it.position == position } -> print("6 ")
		world.gameState.player2.entities.any { it.position == position } -> print("7 ")
		else -> print("${cell.ordinal} ")
	}
}