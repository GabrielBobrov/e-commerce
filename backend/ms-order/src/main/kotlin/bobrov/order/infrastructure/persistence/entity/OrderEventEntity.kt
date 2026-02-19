package bobrov.order.infrastructure.persistence.entity

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    val order: OrderEntity,

    @Column(name = "event_type", nullable = false)
    val eventType: String,

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
    constructor() : this(
        id = null,
        order = OrderEntity(),
        eventType = "",
        payload = emptyMap()
    )
}
