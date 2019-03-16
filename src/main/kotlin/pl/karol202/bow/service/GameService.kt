package pl.karol202.bow.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import org.springframework.stereotype.Service
import pl.karol202.bow.game.DarvinGameManager
import pl.karol202.bow.model.GameState
import javax.annotation.PreDestroy

@Service
class GameService
{
	private val coroutineJob = Job()
	private val coroutineScope = CoroutineScope(coroutineJob)

	private val gameManager = DarvinGameManager(coroutineScope, null)

	fun updateStateAndGetOrder(gameState: GameState.GameStateData) = gameManager.updateStateAndGetOrder(gameState)

	@PreDestroy
	fun onDestroy()
	{
		coroutineJob.cancel()
	}
}