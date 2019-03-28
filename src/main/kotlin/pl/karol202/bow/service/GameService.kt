package pl.karol202.bow.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import org.springframework.stereotype.Service
import pl.karol202.bow.game.DarvinGameListener
import pl.karol202.bow.game.DarvinGameManager
import pl.karol202.bow.model.GameState
import pl.karol202.bow.robot.DataSerializer
import java.io.File
import javax.annotation.PreDestroy

@Service
class GameService
{
	companion object
	{
		const val DATA_FILE_PATH = "network.dat"
	}

	private val coroutineJob = Job()
	private val coroutineScope = CoroutineScope(coroutineJob)

	val dataSerializer = DataSerializer(File(DATA_FILE_PATH))
	private val gameManager = DarvinGameManager(coroutineScope, dataSerializer.loadData())

	fun updateStateAndGetOrder(gameState: GameState.GameStateData) = gameManager.updateStateAndGetOrder(gameState)

	@PreDestroy
	fun onDestroy()
	{
		coroutineJob.cancel()
	}

	fun setGameListener(gameListener: DarvinGameListener)
	{
		gameManager.setGameListener(gameListener)
	}
}