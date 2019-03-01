package ncdc.bow.model

data class Action(val id: String, //Id of entity or base
                  val type: Type,
                  val target: String) //Id of entity, move direction or entity to recruit
{
	enum class Type
	{
		ATTACK, RECRUIT, MOVE, ENTRENCH
	}

	data class ActionData(val actionType: Action.Type? = null,
	                      val id: String? = null,
	                      val target: String? = null)
	{
		fun toAction() = Action(id!!, actionType!!, target!!)
	}
}