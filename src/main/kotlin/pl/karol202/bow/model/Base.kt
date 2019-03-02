package pl.karol202.bow.model

import pl.karol202.bow.game.Game

data class Base(val id: String,
                val owner: Player.Side,
                val position: LocalPosition,
                val hp: Int,
                val newEntity: Entity?)
{
	data class BaseData(val coordinates: ServerPosition? = null,
	                    val hp: Int = 0,
	                    val id: String? = null,
	                    val newUnit: Entity.EntityData? = null,
	                    val owner: Player.Side? = null)
	{
		fun toBase(game: Game) = Base(id!!, owner!!, coordinates!!.toLocalSystem(game), hp, newUnit?.toEntity(game))
	}
}