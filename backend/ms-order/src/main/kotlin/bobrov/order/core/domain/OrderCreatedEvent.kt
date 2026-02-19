package bobrov.order.core.domain

import java.math.BigDecimal
import java.util.UUID

data class OrderCreatedEvent(
    val orderNumber: String,
    val customerId: UUID,
    val items: List<OrderItemEvent>,
    val shippingAddress: Map<String, Any>,
    val notes: String? = null,
    val metadata: Map<String, Any>? = null,
    val totalAmount: BigDecimal? = null
)

data class OrderItemEvent(
    val productId: UUID,
    val productName: String,
    val productSku: String,
    val productImage: String? = null,
    val quantity: Int,
    val unitPrice: BigDecimal,
    val discount: BigDecimal = BigDecimal.ZERO,
    val attributes: Map<String, Any>? = null
)
