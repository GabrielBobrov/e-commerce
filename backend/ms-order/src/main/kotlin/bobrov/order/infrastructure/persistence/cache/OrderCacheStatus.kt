package bobrov.order.infrastructure.persistence.cache

enum class OrderCacheStatus {
    PENDING,
    CONFIRMED,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    FAILED
}