package pl.karol202.bow.model

data class Order(val actions: List<ActionModel>)
{
	companion object
	{
		val emptyOrder = Order(emptyList())
	}

	data class OrderData(val actions: List<ActionModel.ActionData>)

	fun toOrderData() = OrderData(actions.map { it.toActionData() })
}