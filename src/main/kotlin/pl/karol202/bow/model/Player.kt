package pl.karol202.bow.model

import pl.karol202.bow.game.Game

data class Player(val id: Int,
                  val side: Side,
                  val active: Boolean,
                  val base: Base,
                  val gold: Int,
                  val entities: List<Entity>)
{
	enum class Side
	{
		PLAYER1, PLAYER2
	}

	data class PlayerData(val active: Boolean = false,
	                      val base: Base.BaseData? = null,
	                      val gold: Int = 0,
	                      val id: Int = 0,
	                      val side: Side? = null,
	                      val units: List<Entity.EntityData>? = null)
	{
		fun toPlayer(game: Game) = Player(id, side!!, active, base!!.toBase(game), gold, units!!.map { it.toEntity(game) })
	}
}