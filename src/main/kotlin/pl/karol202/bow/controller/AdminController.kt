package pl.karol202.bow.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import pl.karol202.bow.model.Player
import pl.karol202.bow.service.GameService

@RestController
class AdminController
{
	@ResponseStatus(HttpStatus.CONFLICT)
	private class ConflictException : Exception()

	@Autowired
	private lateinit var gameService: GameService

	@PostMapping("/admin/serverAddress")
	fun setServerAddress(address: String)
	{
		GameService.serverAddress = address
	}

	@GetMapping("/admin/actionThreshold")
	fun getActionThreshold() = gameService.actionThreshold

	@PostMapping("/admin/actionThreshold")
	fun setActionThreshold(threshold: Float)
	{
		gameService.actionThreshold = threshold
	}

	@GetMapping("/admin/learnRate")
	fun getLearnRate() = gameService.learnRate

	@PostMapping("/admin/learnRate")
	fun setLearnRate(learnRate: Float)
	{
		gameService.learnRate = learnRate
	}

	@GetMapping("/admin/discountFactor")
	fun getDiscountFactor() = gameService.discountFactor

	@PostMapping("/admin/discountFactor")
	fun setDiscountFactor(factor: Float)
	{
		gameService.discountFactor = factor
	}

	@GetMapping("/admin/epsilon")
	fun getEpsilon() = gameService.epsilon

	@PostMapping("/admin/epsilon")
	fun setEpsilon(epsilon: Float)
	{
		gameService.epsilon = epsilon
	}

	@GetMapping("/admin/samplesLimit")
	fun getLearningSamplesLimit() = gameService.learningSamplesLimit

	@PostMapping("/admin/samplesLimit")
	fun setLearningSamplesLimit(limit: Int)
	{
		gameService.learningSamplesLimit = limit
	}

	@GetMapping("/admin/botsDirectory")
	fun getBotsDirectory() = gameService.botsDirectory

	@PostMapping("/admin/botsDirectory")
	fun setBotsDirectory(directory: String)
	{
		gameService.botsDirectory = directory
	}

	@GetMapping("/admin/samplesDirectory")
	fun getSamplesDirectory() = gameService.samplesDirectory

	@PostMapping("/admin/samplesDirectory")
	fun setSamplesDirectory(directory: String)
	{
		gameService.samplesDirectory = directory
	}

	@GetMapping("/admin/allowBotDuplication")
	fun getAllowBotDuplication() = gameService.allowBotDuplication

	@PostMapping("/admin/allowBotDuplication")
	fun setAllowBotDuplication(allow: Boolean)
	{
		gameService.allowBotDuplication = allow
	}

	@GetMapping("/admin/botBindings")
	fun getBotBindings() = gameService.botBindings

	@PostMapping("/admin/botBindings")
	fun setBotBindings(@RequestParam(required = false) player1: String?, /*@RequestParam(required = false) */player2: String?)
	{
		gameService.botBindings = mapOf(Player.Side.PLAYER1 to player1, Player.Side.PLAYER2 to player2)
				.mapNotNull { (side, bot) -> bot?.let { side to it } }.toMap()
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
	fun teach(samplesAmount: Int) = gameService.teach(samplesAmount)
}