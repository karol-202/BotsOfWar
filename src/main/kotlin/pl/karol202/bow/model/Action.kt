package pl.karol202.bow.model

sealed class Action(private val id: String, //Id of entity or base
                    private val type: Type,
                    private val target: String) //Id of entity, move direction or entity to recruit
{
	enum class Type(val actionCreator: (id: String, target: String) -> Action)
	{
		ATTACK(::AttackAction),
		MOVE(::MoveAction),
		RECRUIT(::RecruitAction),
		ENTRENCH(::EntrenchAction)
	}

	data class ActionData(val id: String? = null,
	                      val actionType: Type? = null,
	                      val target: String? = null)
	{
		fun toAction() = actionType!!.actionCreator(id!!, target!!)
	}

	fun toActionData() = ActionData(id, type, target)
}

class AttackAction(id: String,
                   target: String) : Action(id, Type.ATTACK, target)
{
	constructor(attacker: Entity, victim: Entity) : this(attacker.id, victim.id)
}

class MoveAction(id: String,
                 target: String) : Action(id, Type.MOVE, target)
{
	constructor(entity: Entity, direction: Direction) : this(entity.id, direction.invertVertically().symbol)
}

class RecruitAction(id: String,
                    target: String) : Action(id, Type.RECRUIT, target)
{
	constructor(base: Base, entityType: Entity.Type) : this(base.id, entityType.symbol)
}

class EntrenchAction(id: String,
                     target: String) : Action(id, Type.ENTRENCH, target)
{
	constructor(entity: Entity) : this(entity.id, entity.id)
}