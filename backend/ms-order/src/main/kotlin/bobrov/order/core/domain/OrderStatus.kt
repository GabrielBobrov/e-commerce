package bobrov.order.core.domain

import java.time.LocalDateTime
import java.util.UUID

data class OrderStatus(
    val id: UUID? = null,
    val statusType: String,
    val status: String,
    var isActive: Boolean = true,
    val changedBy: String? = null,
    val reason: String? = null,
    val metadata: Map<String, Any>? = null,
    val createdAt: LocalDateTime = LocalDateTime.now()
)
