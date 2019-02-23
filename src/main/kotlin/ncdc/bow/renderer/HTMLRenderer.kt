package ncdc.bow.renderer

import kotlinx.html.*
import kotlinx.html.stream.appendHTML
import ncdc.bow.model.Entity
import ncdc.bow.model.GameMap
import ncdc.bow.model.LocalPosition
import org.newdawn.slick.util.pathfinding.Path
import java.io.File
import java.io.Writer

class HTMLRenderer(private val writer: Writer) : Renderer
{
	constructor(filePath: String): this(File(filePath).run {
		createNewFile()
		bufferedWriter()
	})

	constructor(file: File): this(file.bufferedWriter())

	override fun render(gameMap: GameMap, path: Path?, coordinates: List<LocalPosition>?, entities: List<Entity>?)
	{
		writer.appendHTML().html {
			head {
				title { + "Mapa" }
			}
			body {
				table {
					gameMap.data.forEachIndexed { y, row ->
						tr {
							row.forEachIndexed { x, cell ->
								td {
									style = "padding: 0;"

									val position = LocalPosition(x, y)
									val isPath = path?.contains(x, y) == true
									val isCoordinate = coordinates?.contains(position) == true
									val entity = entities?.filter { it.position == position } ?: emptyList()

									renderCell(cell, isPath, isCoordinate, entity)
								}
							}
						}
					}
				}
			}
		}.flush()
	}

	private fun FlowContent.renderCell(cell: GameMap.Cell, isPath: Boolean, isCoordinate: Boolean, entities: List<Entity>)
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
			if(isPath) img("PATH", "../../src/main/resources/path.png") {
				positionAndSize(48, 48)
			}
			if(isCoordinate) img("COORD", "../../src/main/resources/coord.png") {
				positionAndSize(48, 48)
			}
			if(entities.isNotEmpty()) renderEntities(entities)
		}
	}

	private fun FlowContent.renderEntities(entities: List<Entity>)
	{
		fun Entity.getImage() = when(type)
		{
			Entity.Type.HORSE -> "../../src/main/resources/entities/horse.png"
			Entity.Type.ARCHER -> "../../src/main/resources/entities/archer.png"
			Entity.Type.WORKER -> "../../src/main/resources/entities/worker.png"
			Entity.Type.WARRIOR -> "../../src/main/resources/entities/warrior.png"
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
		}
	}

	private fun IMG.positionAndSize(width: Int, height: Int, x: Int = 0, y: Int = 0)
	{
		style = "position: absolute; left: $x; top: $y;"
		this.width = "${width}px"
		this.height = "${height}px"
	}
}