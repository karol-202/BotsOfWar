package pl.karol202.bow.util

class Counter(initialValue: Int = 0,
              private val maxValue: Int)
{
	var value = initialValue
		get() = field++.also { if(field > maxValue) field = 0 }
		private set
}