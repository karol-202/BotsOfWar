package pl.karol202.bow.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import pl.karol202.bow.game.DarvinGameListener
import pl.karol202.bow.game.DarvinGameManager
import pl.karol202.bow.model.GameState
import pl.karol202.bow.model.Order
import pl.karol202.bow.model.Player
import pl.karol202.bow.robot.DataSerializer
import java.io.File
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy
import kotlin.system.measureTimeMillis

@Service
class GameService : DarvinGameListener
{
	companion object
	{
		private const val DATA_FILE_PATH = "darvin.dat"

		var serverAddress = "http://192.168.253.121:8080"
	}

	private val coroutineJob = Job()
	private val coroutineScope = CoroutineScope(coroutineJob)

	private val dataSerializer = DataSerializer(File(DATA_FILE_PATH))
	private val gameManager = DarvinGameManager(coroutineScope, dataSerializer.loadData())
	private val logger: Logger = LoggerFactory.getLogger(GameService::class.java)

	@PostConstruct
	fun onStart()
	{
		logger.debug("Debugging starting")
		gameManager.setGameListener(this)
	}

	fun updateStateAndGetOrder(gameState: GameState.GameStateData): Order
	{
		val startTime = System.currentTimeMillis()
		return gameManager.updateStateAndGetOrder(gameState).also {
			val elapsedTime = System.currentTimeMillis() - startTime
			logger.debug("Calculated response in $elapsedTime ms")
		}
	}

	fun removeAgentData(side: Player.Side)
	{
		gameManager.removeAgentData(side)
	}

	fun swapAgentData()
	{
		gameManager.swapAgentsData()
	}

	fun setLearningEnabled(enabled: Boolean)
	{
		gameManager.learningEnabled = enabled
	}

	@PreDestroy
	fun onDestroy()
	{
		coroutineJob.cancel()
	}

	override fun onGameStart()
	{
		logger.info("Game started")
	}

	override fun onUpdate(turn: Int)
	{
		logger.info("Game updated (turn: $turn)")
	}

	override fun onReset()
	{
		logger.warn("Game restarted without teaching")
	}

	override fun onDataUpdate(data: DarvinGameManager.Data)
	{
		logger.info("Data updated")
		saveData(data)
	}

	private fun saveData(data: DarvinGameManager.Data)
	{
		val elapsedTime = measureTimeMillis {
			dataSerializer.saveData(data)
		}
		logger.debug("Saved data in $elapsedTime ms")
	}
}