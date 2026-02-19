package bobrov.order.core.exception

open class BusinessException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
