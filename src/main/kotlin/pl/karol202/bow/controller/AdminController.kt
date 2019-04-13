package pl.karol202.bow.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import pl.karol202.bow.model.Player
import pl.karol202.bow.service.GameService

@RestController
class AdminController
{
	@ResponseStatus(HttpStatus.CONFLICT)
	private class ConflictException : Exception()

	@Autowired
	private lateinit var gameService: GameService

	@GetMapping("/admin/serverAddress")
	fun getServerAddress() = GameService.serverAddress

	@PostMapping("/admin/serverAddress")
	fun setServerAddress(address: String)
	{
		GameService.serverAddress = address
	}

	@GetMapping("/admin/params")
	fun getParams() = gameService.params

	@PostMapping("/admin/params")
	fun setParams(actionThreshold: Float,
	              discountFactor: Float,
	              epsilon: Float,
	              learningSamplesLimit: Int,
	              botsDirectory: String,
	              samplesDirectory: String,
	              allowBotDuplication: Boolean,
	              botBindingForPlayer1: String?,
	              botBindingForPlayer2: String?)
	{
		gameService.params = GameService.Params(actionThreshold,
		                                        discountFactor,
		                                        epsilon,
		                                        learningSamplesLimit,
		                                        botsDirectory,
		                                        samplesDirectory,
		                                        allowBotDuplication,
		                                        mapOf(Player.Side.PLAYER1 to botBindingForPlayer1,
		                                              Player.Side.PLAYER2 to botBindingForPlayer2)
				                                        .mapNotNull { (side, bot) -> bot?.let { side to it } }.toMap())
	}

	@GetMapping("/admin/botsNames")
	fun getBotsNames() = gameService.botsNames

	@GetMapping("/admin/samplesAmount")
	fun getSamplesAmount() = gameService.samplesAmount

	@PostMapping("/admin/newBot")
	fun newBot(name: String, randomRange: Float): ResponseEntity<Nothing> =
			if(gameService.addNewBot(name, randomRange)) ResponseEntity.status(HttpStatus.CREATED).body(null)
			else throw ConflictException()

	@PostMapping("/admin/teach")
	fun teach(learnRate: Float, samplesAmount: Int, bots: Array<String>?) = gameService.teach(learnRate, samplesAmount, bots?.toList())
}