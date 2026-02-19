package bobrov.order.entrypoint.dto

import java.util.UUID

data class CreateOrderRequest(
    val customerId: UUID,
    val items: List<CreateOrderItemRequest>,
    val shippingAddress: Map<String, Any>,
    val notes: String? = null,
    val metadata: Map<String, Any>? = null
)
