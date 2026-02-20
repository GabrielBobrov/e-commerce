package bobrov.order.infrastructure.persistence.entity

import bobrov.order.core.domain.enums.ChangedBy
import bobrov.order.core.domain.enums.OrderState
import bobrov.order.core.domain.enums.OrderStatusType
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "order_statuses")
data class OrderStatusEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    val order: OrderEntity,

    @Enumerated(EnumType.STRING)
    @Column(name = "status_type", nullable = false)
    val statusType: OrderStatusType,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: OrderState,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,

    @Column(name = "changed_by")
    val changedBy: String? = null, // Mantemos String no banco para flexibilidade (Enum ou ID)

    val reason: String? = null,

    @Column(columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    val metadata: Map<String, Any>? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    // Construtor sem argumentos para o Hibernate
    constructor() : this(
        id = null,
        order = OrderEntity(),
        statusType = OrderStatusType.ORDER,
        status = OrderState.PENDING
    )
}
