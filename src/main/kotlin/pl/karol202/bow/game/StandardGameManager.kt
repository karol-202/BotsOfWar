package pl.karol202.bow.game

import pl.karol202.bow.bot.StatelessBot

class StandardGameManager(private val bot: StatelessBot) : GameManager()
{
	override fun createBot() = bot
}