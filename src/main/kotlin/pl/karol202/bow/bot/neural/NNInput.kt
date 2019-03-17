package pl.karol202.bow.bot.neural

import pl.karol202.bow.bot.Action
import pl.karol202.bow.game.Game
import pl.karol202.bow.model.GameState
import pl.karol202.bow.model.LocalPosition
import pl.karol202.bow.util.map

class InputContext
{
	private val inputs = mutableListOf<NNInput>()

	fun add(input: NNInput)
	{
		inputs.add(input)
	}

	fun create(): NNInput = CompoundNNInput(*inputs.toTypedArray())
}

fun compoundInput(block: InputContext.() -> Unit) = InputContext().apply(block).create()

fun InputContext.scalarInput(getValue: (Game, GameState, Action) -> Float) = add(ScalarNNInput(getValue))

fun InputContext.positionInput(getValue: (Game, GameState, Action) -> LocalPosition) = add(PositionNNInput(getValue))

fun InputContext.oneOfNInput(size: Int, getValue: (Game, GameState, Action) -> Int) = add(OneOfNNNInput(size, getValue))

abstract class NNInput
{
	abstract val size: Int //Amount of neurons in input layer

	abstract fun write(array: FloatArray, offset: Int, game: Game, state: GameState, action: Action)
}

private class ScalarNNInput(private val getValue: (Game, GameState, Action) -> Float) : NNInput()
{
	override val size = 1

	override fun write(array: FloatArray, offset: Int, game: Game, state: GameState, action: Action)
	{
		array[offset] = getValue(game, state, action)
	}
}

private class PositionNNInput(private val getValue: (Game, GameState, Action) -> LocalPosition) : NNInput()
{
	override val size = 2

	override fun write(array: FloatArray, offset: Int, game: Game, state: GameState, action: Action)
	{
		val position = getValue(game, state, action)
		array[offset] = position.x.toFloat().map(0f..game.width.toFloat(), -1f..1f)
	}
}

private class OneOfNNNInput(override val size: Int,
                            private val getValue: (Game, GameState, Action) -> Int) : NNInput()
{
	override fun write(array: FloatArray, offset: Int, game: Game, state: GameState, action: Action)
	{
		val which = getValue(game, state, action)
		(offset until offset + size).forEachIndexed { ordinal, arrayOffset ->
			array[arrayOffset] = if(which == ordinal) 1f else 0f
		}
	}
}

private class CompoundNNInput(private vararg val children: NNInput) : NNInput()
{
	override val size = children.sumBy { it.size }

	override fun write(array: FloatArray, offset: Int, game: Game, state: GameState, action: Action)
	{
		var currentOffset = offset
		children.forEach {
			it.write(array, currentOffset, game, state, action)
			currentOffset += it.size
		}
	}
}
