package pl.karol202.bow

open class APIException(message: String, cause: Throwable? = null) : Exception(message, cause)