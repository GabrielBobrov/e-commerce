package bobrov.order.infrastructure.persistence.entity

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "orders")
data class OrderEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(name = "order_number", nullable = false, unique = true)
    val orderNumber: String,

    @Column(name = "customer_id", nullable = false)
    val customerId: UUID,

    @Column(nullable = false)
    val subtotal: BigDecimal,

    @Column(nullable = false)
    val tax: BigDecimal = BigDecimal.ZERO,

    @Column(name = "shipping_fee", nullable = false)
    val shippingFee: BigDecimal = BigDecimal.ZERO,

    @Column(nullable = false)
    val discount: BigDecimal = BigDecimal.ZERO,

    @Column(name = "total_amount", nullable = false)
    val totalAmount: BigDecimal,

    @Column(name = "shipping_address", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    val shippingAddress: Map<String, Any>,

    @Column(name = "payment_id")
    val paymentId: String? = null,

    @Column(name = "tracking_code")
    val trackingCode: String? = null,

    val notes: String? = null,

    @Column(columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    val metadata: Map<String, Any>? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @Version
    val version: Int = 0,

    @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL], orphanRemoval = true)
    val items: MutableList<OrderItemEntity> = mutableListOf(),

    @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL], orphanRemoval = true)
    val statuses: MutableList<OrderStatusEntity> = mutableListOf()
) {
    constructor() : this(
        id = null,
        orderNumber = "",
        customerId = UUID.randomUUID(),
        subtotal = BigDecimal.ZERO,
        totalAmount = BigDecimal.ZERO,
        shippingAddress = emptyMap()
    )

    @PreUpdate
    fun onUpdate() {
        updatedAt = LocalDateTime.now()
    }
}