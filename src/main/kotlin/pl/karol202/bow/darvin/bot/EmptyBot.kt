package pl.karol202.bow.darvin.bot

import pl.karol202.bow.game.Game
import pl.karol202.bow.model.Order
import pl.karol202.bow.model.Player

class EmptyBot : Bot
{
	override fun play(game: Game, side: Player.Side) = Order.emptyOrder

	override fun endGame(game: Game, winner: Player.Side) { }
}