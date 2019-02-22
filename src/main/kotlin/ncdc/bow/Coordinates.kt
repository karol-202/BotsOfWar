package ncdc.bow

import org.springframework.web.client.RestTemplate

object Coordinates
{
	private const val ENDPOINT = "http://bow.westeurope.cloudapp.azure.com:8080/getCoordinatesList"

	fun fromServer(world: World) =
			RestTemplate().getForObject(ENDPOINT, Array<Array<Int>>::class.java)?.map {
				ServerPosition(it[0], it[1]).toLocalSystem(world)
			} ?: throw APIException("Cannot fetch coordinates")
}