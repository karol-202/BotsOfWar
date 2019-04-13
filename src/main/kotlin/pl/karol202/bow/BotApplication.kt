package pl.karol202.bow

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
open class BotApplication

fun main(args: Array<String>)
{
	runApplication<BotApplication>(*args)
}