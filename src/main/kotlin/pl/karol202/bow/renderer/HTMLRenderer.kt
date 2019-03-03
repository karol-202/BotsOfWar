package pl.karol202.bow.renderer

import kotlinx.html.*
import kotlinx.html.stream.appendHTML
import pl.karol202.bow.game.Game
import pl.karol202.bow.model.Entity
import pl.karol202.bow.model.GameMap
import pl.karol202.bow.model.LocalPosition
import pl.karol202.bow.model.Player
import java.io.File
import java.io.Writer

class HTMLRenderer(private val writer: Writer) : Renderer
{
	constructor(filePath: String): this(File(filePath).run {
		createNewFile()
		bufferedWriter()
	})

	override fun render(game: Game)
	{
		writer.appendHTML().html {
			head {
				title { + "Mapa" }
			}
			body {
				table {
					game.gameMap.data.forEachIndexed { y, row ->
						tr {
							row.forEachIndexed { x, cell ->
								td {
									style = "padding: 0;"

									val position = LocalPosition(x, y)
									val entities = game.allEntities.filter { it.position == position }

									renderCell(cell, entities)
								}
							}
						}
					}
				}
			}
		}.flush()
	}

	private fun FlowContent.renderCell(cell: GameMap.Cell, entities: List<Entity>)
	{
		fun GameMap.Cell.getImage() = when(this)
		{
			GameMap.Cell.DIRT -> "../../src/main/resources/tiles/dirt.png"
			GameMap.Cell.GRASS -> "../../src/main/resources/tiles/grass.png"
			GameMap.Cell.WATER -> "../../src/main/resources/tiles/water.png"
			GameMap.Cell.ROCK -> "../../src/main/resources/tiles/rock.png"
			GameMap.Cell.BASE -> "../../src/main/resources/tiles/base.png"
			GameMap.Cell.MINE -> "../../src/main/resources/tiles/mine.png"
		}

		div {
			style = "width: 48px; height: 48px; position: relative;"
			img(cell.name, cell.getImage()) {
				positionAndSize(48, 48)
			}
			if(entities.isNotEmpty()) renderEntities(entities)
		}
	}

	private fun FlowContent.renderEntities(entities: List<Entity>)
	{
		fun Entity.getImage() = when(type)
		{
			Entity.Type.HORSE -> if(owner == Player.Side.PLAYER1) "../../src/main/resources/entities/horse_red.png"
								  else "../../src/main/resources/entities/horse_blue.png"
			Entity.Type.ARCHER -> if(owner == Player.Side.PLAYER1) "../../src/main/resources/entities/archer_red.png"
								   else "../../src/main/resources/entities/archer_blue.png"
			Entity.Type.WORKER -> if(owner == Player.Side.PLAYER1) "../../src/main/resources/entities/worker_red.png"
								   else "../../src/main/resources/entities/worker_blue.png"
			Entity.Type.WARRIOR -> if(owner == Player.Side.PLAYER1) "../../src/main/resources/entities/warrior_red.png"
									else "../../src/main/resources/entities/warrior_blue.png"
		}

		fun entity(entity: Entity, size: Int)
		{
			img(entity.type.name, entity.getImage()) {
				positionAndSize(size, size, 0, 0)
			}
		}

		when(entities.size)
		{
			0 -> return
			1 -> entity(entities[0], 48)
			in 2..4 -> table {
				tr {
					td {
						entity(entities[0], 24)
					}
					td {
						entity(entities[1], 24)
					}
				}
				tr {
					td {
						entity(entities[2], 24)
					}
					td {
						entity(entities[3], 24)
					}
				}
			}
			else -> println("Could not display entities: ${entities.size}")
		}
	}

	private fun IMG.positionAndSize(width: Int, height: Int, x: Int = 0, y: Int = 0)
	{
		style = "position: absolute; left: $x; top: $y;"
		this.width = "${width}px"
		this.height = "${height}px"
	}
}