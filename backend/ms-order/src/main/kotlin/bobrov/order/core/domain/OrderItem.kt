package bobrov.order.core.domain

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class OrderItem(
    val id: UUID? = null,
    val productId: UUID,
    val productName: String,
    val productSku: String,
    val productImage: String? = null,
    val quantity: Int,
    val unitPrice: BigDecimal,
    val discount: BigDecimal = BigDecimal.ZERO,
    val subtotal: BigDecimal,
    val attributes: Map<String, Any>? = null,
    val createdAt: LocalDateTime = LocalDateTime.now()
)
