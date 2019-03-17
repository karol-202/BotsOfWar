package pl.karol202.bow.util

typealias FloatRange = ClosedFloatingPointRange<Float>

fun Float.mapAndClamp(from: FloatRange, to: FloatRange) = map(from, to).coerceIn(to.start, to.endInclusive)

fun Float.map(from: FloatRange, to: FloatRange) = to.lerp(from.invertedLerp(this))

fun FloatRange.lerp(factor: Float) = start + (factor * size)

fun FloatRange.invertedLerp(value: Float) = (value - start) / size

val FloatRange.size get() = endInclusive - start
