package bobrov.order.entrypoint.dto

import java.math.BigDecimal
import java.util.UUID

data class OrderItemResponse(
    val id: UUID,
    val productId: UUID,
    val productName: String,
    val quantity: Int,
    val unitPrice: BigDecimal,
    val subtotal: BigDecimal
)
