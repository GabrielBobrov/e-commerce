package bobrov.order.core.domain

import bobrov.order.core.domain.enums.ChangedBy
import bobrov.order.core.domain.enums.OrderState
import bobrov.order.core.domain.enums.OrderStatusType
import java.time.LocalDateTime
import java.util.UUID

data class OrderStatus(
    val id: UUID? = null,
    val statusType: OrderStatusType,
    val status: OrderState,
    var isActive: Boolean = true,
    val changedBy: ChangedBy? = null, // Pode ser null se não for um dos enums padrão (ex: ID de usuário)
    val changedByUserId: String? = null, // Campo opcional para ID específico se não for Enum
    val reason: String? = null,
    val metadata: Map<String, Any>? = null,
    val createdAt: LocalDateTime = LocalDateTime.now()
)
