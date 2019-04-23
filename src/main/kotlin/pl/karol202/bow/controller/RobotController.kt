package pl.karol202.bow.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import pl.karol202.bow.service.RobotService

@RestController
class RobotController
{
	@Autowired
	private lateinit var robotService: RobotService

	@GetMapping("/robot/params")
	fun getParams() = robotService.params

	@PostMapping("/robot/params")
	fun setParams(learnRate: Float?,
	              samplesPerEpoch: Int?)
	{
		val oldParams = robotService.params
		robotService.params = RobotService.Params(learnRate ?: oldParams.learnRate,
		                                          samplesPerEpoch ?: oldParams.samplesPerEpoch)
	}

	@PostMapping("/robot/newGame")
	fun newGame() = robotService.newGame()

	@PostMapping("/robot/initTeaching")
	fun initTeaching(bot1Name: String, bot1WeightRange: Float,
	                 bot2Name: String, bot2WeightRange: Float) =
			robotService.initTeaching(bot1Name, bot2Name, bot1WeightRange, bot2WeightRange)

	@PostMapping("/robot/startTeaching")
	fun startTeaching() = robotService.startTeaching()

	@PostMapping("/robot/stopTeaching")
	fun stopTeaching() = robotService.stopTeaching()
}