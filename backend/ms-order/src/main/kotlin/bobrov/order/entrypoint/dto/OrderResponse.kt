package bobrov.order.entrypoint.dto

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class OrderResponse(
    val id: UUID,
    val orderNumber: String,
    val customerId: UUID,
    val subtotal: BigDecimal,
    val tax: BigDecimal,
    val shippingFee: BigDecimal,
    val discount: BigDecimal,
    val totalAmount: BigDecimal,
    val shippingAddress: Map<String, Any>,
    val status: String?,
    val items: List<OrderItemResponse>,
    val createdAt: LocalDateTime
)
