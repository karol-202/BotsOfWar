package pl.karol202.bow.model

import pl.karol202.bow.game.Game

data class GameState(val gameId: Int,
                     val tournamentId: Int,
                     val currentTurn: Int,
                     val currentStep: Int, //Every turn is 2 steps
                     val mines: List<Mine>,
                     val player1: Player,
                     val player2: Player,
                     val lastActions: List<ActionModel>,
                     val lastLogs: List<String>)
{
	data class GameStateData(val currentStep: Int = 0,
	                         val currentTurn: Int = 0,
	                         val gameId: Int = 0,
	                         val lastActions: List<ActionModel.ActionData>? = null,
	                         val lastLogs: List<String>? = null,
	                         var mapPath: String? = null, //Server address will be added as a prefix by controller
	                         val mines: List<Mine.MineData>? = null,
	                         val player1: Player.PlayerData? = null,
	                         val player2: Player.PlayerData? = null,
	                         val tournamentId: Int = 0)
	{
		fun toGameState(game: Game) =
				GameState(gameId, tournamentId, currentTurn, currentStep, mines!!.map { it.toMine(game) },
				          player1!!.toPlayer(game, Player.Side.PLAYER1), player2!!.toPlayer(game, Player.Side.PLAYER2),
				          lastActions?.map { it.toAction() } ?: emptyList(), lastLogs ?: emptyList())
	}

	private val players get() = listOf(player1, player2)
	val activePlayer get() = players.single { it.active }
	val allEntities get() = players.flatMap { it.entities }

	fun getPlayer(side: Player.Side) = when(side)
	{
		Player.Side.PLAYER1 -> player1
		Player.Side.PLAYER2 -> player2
	}

	fun getEntitiesAt(position: LocalPosition) = allEntities.filter { it.position == position }

	fun getEntityById(entityId: String, player: Player? = null) =
			(player?.entities ?: allEntities).single { it.id == entityId }

	fun getMineAt(position: LocalPosition) = mines.singleOrNull { it.position == position }
}