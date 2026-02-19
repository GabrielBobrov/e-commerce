package bobrov.order.core.domain

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class Order(
    val id: UUID? = null,
    val orderNumber: String,
    val customerId: UUID,
    val subtotal: BigDecimal,
    val tax: BigDecimal = BigDecimal.ZERO,
    val shippingFee: BigDecimal = BigDecimal.ZERO,
    val discount: BigDecimal = BigDecimal.ZERO,
    val totalAmount: BigDecimal,
    val shippingAddress: Map<String, Any>,
    val paymentId: String? = null,
    val trackingCode: String? = null,
    val notes: String? = null,
    val metadata: Map<String, Any>? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime = LocalDateTime.now(),
    val version: Int = 0,
    val items: MutableList<OrderItem> = mutableListOf(),
    val statuses: MutableList<OrderStatus> = mutableListOf(),
    val events: MutableList<OrderEvent> = mutableListOf()
)
