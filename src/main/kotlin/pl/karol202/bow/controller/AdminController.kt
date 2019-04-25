package pl.karol202.bow.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import pl.karol202.bow.darvin.environment.StandardEnvironment
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
	fun setParams(actionThreshold: Float?,
	              discountFactor: Float?,
	              epsilon: Float?,
	              baseReward: Float?,
	              winReward: Float?,
	              lossReward: Float?,
	              myBaseAttackReward: Float?,
	              enemyBaseAttackReward: Float?,
	              myEntityKillReward: Float?,
	              enemyEntityKillReward: Float?,
	              entityRecruitReward: Float?,
	              goldFindReward: Float?,
	              learningSamplesLimit: Int?,
	              botsDirectory: String?,
	              samplesDirectory: String?,
	              allowBotDuplication: Boolean?,
	              botBindingForPlayer1: String?, // Blank to reset
	              botBindingForPlayer2: String?, // Blank to reset
	              collectSamples: Boolean?)
	{
		val oldGame = gameService.params

		val oldEnvironment = oldGame.environmentParams
		val newEnvironment = StandardEnvironment.Params(baseReward ?: oldEnvironment.baseReward,
		                                                winReward ?: oldEnvironment.winReward,
		                                                lossReward ?: oldEnvironment.lossReward,
		                                                myBaseAttackReward ?: oldEnvironment.myBaseAttackReward,
		                                                enemyBaseAttackReward ?: oldEnvironment.enemyBaseAttackReward,
		                                                myEntityKillReward ?: oldEnvironment.myEntityKillReward,
		                                                enemyEntityKillReward ?: oldEnvironment.enemyEntityKillReward,
		                                                entityRecruitReward ?: oldEnvironment.entityRecruitReward,
		                                                goldFindReward ?: oldEnvironment.goldFindReward)

		val oldBindings = oldGame.botBindings
		val newBindings = mapOf(Player.Side.PLAYER1 to (botBindingForPlayer1 ?: oldBindings[Player.Side.PLAYER1]),
		                        Player.Side.PLAYER2 to (botBindingForPlayer2 ?: oldBindings[Player.Side.PLAYER2]))
				.mapNotNull { (side, bot) -> bot?.takeIf { it.isNotBlank() }?.let { side to it } }
				.toMap()

		gameService.params = GameService.Params(actionThreshold ?: oldGame.actionThreshold,
		                                        discountFactor ?: oldGame.discountFactor,
		                                        epsilon ?: oldGame.epsilon,
		                                        newEnvironment,
		                                        learningSamplesLimit ?: oldGame.learningSamplesLimit,
		                                        botsDirectory ?: oldGame.botsDirectory,
		                                        samplesDirectory ?: oldGame.samplesDirectory,
		                                        allowBotDuplication ?: oldGame.allowBotDuplication,
		                                        newBindings,
		                                        collectSamples ?: oldGame.collectSamples)
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