package ncdc.bow.model

import ncdc.bow.World

data class GameState(val gameId: Int,
                     val tournamentId: Int,
                     val currentTurn: Int,
                     val currentStep: Int, //Every turn is 2 steps
                     val mapPath: String,
                     val mines: List<Mine>,
                     val player1: Player,
                     val player2: Player,
                     val lastActions: List<Action>,
                     val lastLogs: List<String>)
{
	data class GameStateData(val currentStep: Int = 0,
	                         val currentTurn: Int = 0,
	                         val gameId: Int = 0,
	                         val lastActions: List<Action.ActionData>? = null,
	                         val lastLogs: List<String>? = null,
	                         val mapPath: String? = null,
	                         val mines: List<Mine.MineData>? = null,
	                         val player1: Player.PlayerData? = null,
	                         val player2: Player.PlayerData? = null,
	                         val tournamentId: Int = 0)
	{
		fun toGameState(world: World) =
				GameState(gameId, tournamentId, currentTurn, currentStep, mapPath!!,
				          mines!!.map { it.toMine(world) }, player1!!.toPlayer(world), player2!!.toPlayer(world),
				          lastActions!!.map { it.toAction() }, lastLogs!!)
	}
}