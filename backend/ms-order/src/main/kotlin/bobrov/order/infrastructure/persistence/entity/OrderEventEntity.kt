package bobrov.order.infrastructure.persistence.entity

import bobrov.order.core.domain.enums.OrderEventType
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "order_events")
data class OrderEventEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(name = "order_id", nullable = true) // Agora é apenas um UUID, sem FK, e pode ser nulo se o pedido não existir ainda
    val orderId: UUID? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    val eventType: OrderEventType,

    @Column(name = "aggregate_type", nullable = false)
    val aggregateType: String = "Order",

    @Column(columnDefinition = "jsonb", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    val payload: Map<String, Any>,

    @Column(nullable = false)
    var published: Boolean = false,

    @Column(name = "published_at")
    var publishedAt: LocalDateTime? = null,

    @Column(name = "sns_message_id")
    var snsMessageId: String? = null,

    @Column(name = "retry_count", nullable = false)
    var retryCount: Int = 0,

    @Column(name = "last_error")
    var lastError: String? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    // Construtor sem argumentos para o Hibernate
    constructor() : this(
        id = null,
        orderId = null,
        eventType = OrderEventType.ORDER_CREATION_REQUESTED,
        payload = emptyMap()
    )
}
