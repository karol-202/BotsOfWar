package pl.karol202.bow.util

class Counter(val value: Int = 0,
              private val maxValue: Int)
{
	val next get() = Counter(getNextValue(), maxValue)
	val zero get() = Counter(0, maxValue)

	private fun getNextValue() = (value + 1).takeIf { it <= maxValue } ?: 0
}