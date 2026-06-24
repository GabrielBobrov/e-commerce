package bobrov.order.core.domain

import java.util.UUID

data class OrderCreatedEvent(
    val orderId: UUID,
    val orderNumber: String = "",
    val customerId: UUID,
    val shippingAddress: Map<String, Any>,
    val notes: String?,
    val metadata: Map<String, Any>?,
    val items: List<OrderItemPayload>
)

data class OrderItemPayload(
    val productId: UUID,
    val productName: String,
    val productSku: String,
    val productImage: String?,
    val quantity: Int,
    val unitPrice: java.math.BigDecimal,
    val discount: java.math.BigDecimal,
    val attributes: Map<String, Any>?
)