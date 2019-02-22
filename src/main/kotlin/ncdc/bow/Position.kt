package ncdc.bow

//Server system: (0, 0) in left bottom corner
data class ServerPosition(val x: Int = 0,
                          val y: Int = 0)
{
	fun toLocalSystem(world: World) = LocalPosition(x, world.height - y - 1)
}

//Local system: (0, 0) in left top corner
data class LocalPosition(val x: Int = 0,
                         val y: Int = 0)
{
	fun toServerSystem(world: World) = ServerPosition(x, world.height - y - 1)
}
