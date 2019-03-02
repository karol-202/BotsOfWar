package pl.karol202.bow.bot

import pl.karol202.bow.game.Game
import pl.karol202.bow.model.Action
import pl.karol202.bow.model.Player

class StandardBot : StatelessBot
{
	override fun play(game: Game, side: Player.Side): List<Action>
	{
		return emptyList()
	}
}