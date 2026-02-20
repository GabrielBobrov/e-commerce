package bobrov.order.core.ports.out

import bobrov.order.core.domain.Order
import bobrov.order.core.domain.OrderEvent
import java.util.UUID

interface IOrderRepositoryPort {
    fun save(order: Order): Order
    fun findById(id: UUID): Order?
    fun findAll(): List<Order>
    fun findByOrderNumber(orderNumber: String): Order?
    fun findByCustomerId(customerId: UUID): List<Order>
    
    fun saveEvent(event: OrderEvent): OrderEvent
    fun findUnpublishedEvents(): List<OrderEvent>
}
