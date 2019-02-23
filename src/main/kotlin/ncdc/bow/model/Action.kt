package ncdc.bow.model

data class Action(val type: Type,
                  val id: String,
                  val target: String)
{
	enum class Type
	{
		ATTACK, RECRUIT, MOVE, ENTRENCH
	}

	data class ActionData(val actionType: Action.Type? = null,
	                              val id: String? = null,
	                              val target: String? = null)
	{
		fun toAction() = Action(actionType!!, id!!, target!!)
	}
}