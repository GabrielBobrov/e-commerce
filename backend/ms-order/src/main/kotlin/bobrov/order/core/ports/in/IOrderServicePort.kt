package bobrov.order.core.ports.`in`

import bobrov.order.core.domain.Order
import java.util.UUID

interface IOrderServicePort {
    fun getAllOrders(): List<Order>
    fun getOrderById(id: UUID): Order
    fun createOrder(order: Order)
}
