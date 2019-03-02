package pl.karol202.bow.bot

import pl.karol202.bow.game.Game
import pl.karol202.bow.model.Action
import pl.karol202.bow.model.Player

interface Bot
{
	fun play(game: Game, side: Player.Side): List<Action>
}

interface StatelessBot : Bot
