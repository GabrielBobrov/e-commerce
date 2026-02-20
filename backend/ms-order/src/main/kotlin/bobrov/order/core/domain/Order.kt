package bobrov.order.core.domain

import bobrov.order.core.domain.enums.ChangedBy
import bobrov.order.core.domain.enums.OrderState
import bobrov.order.core.domain.enums.OrderStatusType
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class Order(
    val id: UUID? = null,
    val orderNumber: String,
    val customerId: UUID,
    val subtotal: BigDecimal = BigDecimal.ZERO,
    val tax: BigDecimal = BigDecimal.ZERO,
    val shippingFee: BigDecimal = BigDecimal.ZERO,
    val discount: BigDecimal = BigDecimal.ZERO,
    val totalAmount: BigDecimal = BigDecimal.ZERO,
    val shippingAddress: Map<String, Any>,
    val paymentId: String? = null,
    val trackingCode: String? = null,
    val notes: String? = null,
    val metadata: Map<String, Any>? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime = LocalDateTime.now(),
    val version: Int = 0,
    val items: MutableList<OrderItem> = mutableListOf(),
    val statuses: MutableList<OrderStatus> = mutableListOf(),
    val events: MutableList<OrderEvent> = mutableListOf()
) {
    fun initializeOrder(): Order {
        // 1. Calcular subtotais dos itens
        val calculatedItems = items.map { it.calculateSubtotal() }.toMutableList()
        
        // 2. Calcular totais do pedido
        val newSubtotal = calculatedItems.sumOf { it.subtotal }
        val newTotalAmount = newSubtotal.add(tax).add(shippingFee).subtract(discount)
        
        // 3. Gerar Order Number se vazio
        val newOrderNumber = orderNumber.ifBlank {
            UUID.randomUUID().toString().substring(0, 8).uppercase()
        }

        // 4. Adicionar status inicial
        val initialStatus = OrderStatus(
            statusType = OrderStatusType.ORDER,
            status = OrderState.PENDING,
            isActive = true,
            changedBy = ChangedBy.SYSTEM,
            reason = "Order created"
        )
        
        val newStatuses = mutableListOf(initialStatus)

        return this.copy(
            orderNumber = newOrderNumber,
            items = calculatedItems,
            subtotal = newSubtotal,
            totalAmount = newTotalAmount,
            statuses = newStatuses
        )
    }
    
    fun addEvent(event: OrderEvent) {
        this.events.add(event)
    }
}
