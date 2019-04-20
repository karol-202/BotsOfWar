package pl.karol202.bow.model

data class NewGameModel(val gameID: Int,
                        val player1Url: String,
                        val player1ID: Int,
                        val player2Url: String,
                        val player2ID: Int,
                        val timeout: Boolean)