package bobrov.order.core.domain

import bobrov.order.core.domain.enums.OrderEventType
import java.time.LocalDateTime
import java.util.UUID

data class OrderEvent(
    val id: UUID? = null,
    val eventType: OrderEventType,
    val aggregateType: String = "Order",
    val payload: Map<String, Any>,
    var published: Boolean = false,
    var publishedAt: LocalDateTime? = null,
    var snsMessageId: String? = null,
    var retryCount: Int = 0,
    var lastError: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now()
)
