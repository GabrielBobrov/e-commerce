package bobrov.order.entrypoint.dto

import java.math.BigDecimal
import java.util.UUID

data class CreateOrderItemRequest(
    val productId: UUID,
    val productName: String,
    val productSku: String,
    val productImage: String? = null,
    val quantity: Int,
    val unitPrice: BigDecimal,
    val discount: BigDecimal? = BigDecimal.ZERO,
    val attributes: Map<String, Any>? = null
)
