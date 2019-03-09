package pl.karol202.bow.game

import pl.karol202.bow.bot.Bot

class StandardGameManager(private val bot: Bot) : GameManager()
{
	override fun createBot() = bot
}