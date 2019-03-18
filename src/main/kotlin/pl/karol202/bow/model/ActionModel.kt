package pl.karol202.bow.model

sealed class ActionModel(private val id: String, //Id of entity or base
                         val type: Type,
                         private val target: String) //Id of entity, move direction or entity to recruit
{
	enum class Type(val actionModelCreator: (id: String, target: String) -> ActionModel)
	{
		ATTACK(::AttackActionModel),
		MOVE(::MoveActionModel),
		RECRUIT(::RecruitActionModel),
		ENTRENCH(::EntrenchActionModel)
	}

	data class ActionData(val id: String? = null,
	                      val actionType: Type? = null,
	                      val target: String? = null)
	{
		fun toAction() = actionType!!.actionModelCreator(id!!, target!!)
	}

	fun toActionData() = ActionData(id, type, target)
}

class AttackActionModel(id: String,
                        target: String) : ActionModel(id, Type.ATTACK, target)
{
	constructor(attacker: Entity, victim: Entity) : this(attacker.id, victim.id)

	constructor(attacker: Entity, victim: Base) : this(attacker.id, victim.id)
}

class MoveActionModel(id: String,
                      target: String) : ActionModel(id, Type.MOVE, target)
{
	constructor(entity: Entity, direction: Direction) : this(entity.id, direction.symbol)
}

class EntrenchActionModel(id: String,
                          target: String) : ActionModel(id, Type.ENTRENCH, target)
{
	constructor(entity: Entity) : this(entity.id, entity.id)
}

class RecruitActionModel(id: String,
                         target: String) : ActionModel(id, Type.RECRUIT, target)
{
	constructor(base: Base, entityType: Entity.Type) : this(base.id, entityType.symbol)
}
