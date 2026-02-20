package bobrov.order.core.domain.enums

enum class OrderEventType {
    ORDER_CREATION_REQUESTED,
    ORDER_CREATED,
    ORDER_UPDATED,
    ORDER_CANCELLED,
    ORDER_PAID,
    ORDER_SHIPPED
}
