package bobrov.order.infrastructure.persistence.cache

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class OrderCache(
    val id: UUID,
    val orderNumber: String,
    val customerId: UUID,
    val totalAmount: BigDecimal,
    val status: OrderCacheStatus?,
    val items: List<OrderItemCache>,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class OrderItemCache(
    val id: UUID,
    val productId: UUID,
    val productName: String,
    val productSku: String,
    val quantity: Int,
    val unitPrice: BigDecimal,
    val subtotal: BigDecimal
)