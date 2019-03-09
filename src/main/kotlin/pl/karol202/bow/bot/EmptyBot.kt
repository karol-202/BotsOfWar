package pl.karol202.bow.bot

import pl.karol202.bow.game.Game
import pl.karol202.bow.model.Order
import pl.karol202.bow.model.Player

class EmptyBot : Bot
{
	override fun play(game: Game, side: Player.Side) = Order.emptyOrder
}