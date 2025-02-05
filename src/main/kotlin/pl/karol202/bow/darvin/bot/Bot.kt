package pl.karol202.bow.darvin.bot

import pl.karol202.bow.game.Game
import pl.karol202.bow.model.Order
import pl.karol202.bow.model.Player

interface Bot<D : Bot.Data>
{
	interface Data

	val data: D

	suspend fun play(game: Game, side: Player.Side): Order

	fun endGame(game: Game, winner: Player.Side)
}
