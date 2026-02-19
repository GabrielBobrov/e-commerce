package bobrov.order.infrastructure.persistence.entity

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "order_items")
data class OrderItemEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    val order: OrderEntity,

    @Column(name = "product_id", nullable = false)
    val productId: UUID,

    @Column(name = "product_name", nullable = false)
    val productName: String,

    @Column(name = "product_sku", nullable = false)
    val productSku: String,

    @Column(name = "product_image")
    val productImage: String? = null,

    @Column(nullable = false)
    val quantity: Int,

    @Column(name = "unit_price", nullable = false)
    val unitPrice: BigDecimal,

    @Column(nullable = false)
    val discount: BigDecimal = BigDecimal.ZERO,

    @Column(nullable = false)
    val subtotal: BigDecimal,

    @Column(columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    val attributes: Map<String, Any>? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    constructor() : this(
        id = null,
        order = OrderEntity(),
        productId = UUID.randomUUID(),
        productName = "",
        productSku = "",
        quantity = 0,
        unitPrice = BigDecimal.ZERO,
        subtotal = BigDecimal.ZERO
    )
}
