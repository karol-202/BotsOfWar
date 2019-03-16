package pl.karol202.bow.bot.neural

import pl.karol202.bow.bot.Action
import pl.karol202.bow.model.GameState

class InputContext
{
	private val inputs = mutableListOf<NNInput>()

	fun add(input: NNInput)
	{
		inputs.add(input)
	}

	fun create(): NNInput = CompoundNNInput(*inputs.toTypedArray())
}

fun compoundInput(block: InputContext.() -> Unit) = InputContext().compoundInput(block)

fun InputContext.compoundInput(block: InputContext.() -> Unit) = apply(block).create()

fun InputContext.scalarInput(getValue: (GameState, Action) -> Float) = add(ScalarNNInput(getValue))

fun InputContext.oneOfNInput(size: Int, getValue: (GameState, Action) -> Int) = add(OneOfNNNInput(size, getValue))

abstract class NNInput
{
	abstract val size: Int //Amount of neurons in input layer

	abstract fun write(array: FloatArray, offset: Int, state: GameState, action: Action)
}

private class ScalarNNInput(private val getValue: (GameState, Action) -> Float) : NNInput()
{
	override val size = 1

	override fun write(array: FloatArray, offset: Int, state: GameState, action: Action)
	{
		array[offset] = getValue(state, action)
	}
}

private class OneOfNNNInput(override val size: Int,
                            private val getValue: (GameState, Action) -> Int) : NNInput()
{
	override fun write(array: FloatArray, offset: Int, state: GameState, action: Action)
	{
		val which = getValue(state, action)
		(offset until offset + size).forEachIndexed { ordinal, arrayOffset ->
			array[arrayOffset] = if(which == ordinal) 1f else 0f
		}
	}
}

private class CompoundNNInput(private vararg val children: NNInput) : NNInput()
{
	override val size = children.sumBy { it.size }

	override fun write(array: FloatArray, offset: Int, state: GameState, action: Action)
	{
		var currentOffset = offset
		children.forEach {
			it.write(array, currentOffset, state, action)
			currentOffset += it.size
		}
	}
}
