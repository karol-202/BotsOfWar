package pl.karol202.bow.model

import pl.karol202.bow.game.Game

data class Entity(val id: String,
                  val owner: Player.Side?,
                  val type: Type,
                  val position: LocalPosition,
                  val hp: Int,
                  val actionPoints: Int,
                  val rangeOfAttack: Int,
                  val damage: Int,
                  val cost: Int,
                  val entrench: Boolean)
{
	enum class Type(val symbol: String)
	{
		HORSE("H"),
		ARCHER("A"),
		WORKER("W"),
		WARRIOR("F")
	}

	data class EntityData(val id: String = "",
	                      val coordinates: ServerPosition? = null,
	                      val owner: Player.Side? = null,
	                      val hp: Int = 0,
	                      val name: Type? = null,
	                      val rangeOfAttack: Int = 0,
	                      val actionPoints: Int = 0,
	                      val damage: Int = 0,
	                      val cost: Int = 0,
	                      val entrench: Boolean = false)
	{
		fun toEntity(game: Game) = Entity(id, owner, name!!, coordinates?.toLocalSystem(game)
				?: LocalPosition(), hp, actionPoints, rangeOfAttack, damage, cost, entrench)
	}
}