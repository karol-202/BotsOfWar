package pl.karol202.bow.util

fun <T> Collection<T>.randomOrNull() = takeIf { isNotEmpty() }?.random()