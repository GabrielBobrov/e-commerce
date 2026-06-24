package bobrov.order.core.adapter.service

import bobrov.order.core.domain.Order
import bobrov.order.core.domain.OrderEvent
import bobrov.order.core.domain.enums.OrderEventType
import bobrov.order.core.ports.`in`.IOrderServicePort
import bobrov.order.core.ports.out.IOrderRepositoryPort
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class OrderService(
    private val orderRepository: IOrderRepositoryPort
) : IOrderServicePort {

    private val logger = LoggerFactory.getLogger(OrderService::class.java)

    @Transactional(readOnly = true)
    override fun getAllOrders(): List<Order> {
        return orderRepository.findAll()
    }

    @Transactional(readOnly = true)
    override fun getOrderById(id: UUID): Order {
        return orderRepository.findById(id) ?: throw RuntimeException("Order not found")
    }

    @Transactional
    override fun createOrder(order: Order) {
        logger.info("[SERVICE] Received order creation request for customer: {}", order.customerId)

        val orderId = UUID.randomUUID()
        val orderNumber = order.orderNumber.ifBlank { UUID.randomUUID().toString().substring(0, 8).uppercase() }

        val eventPayload = mapOf(
            "orderId" to orderId.toString(),
            "orderNumber" to orderNumber,
            "customerId" to order.customerId.toString(),
            "shippingAddress" to order.shippingAddress,
            "notes" to (order.notes ?: ""),
            "metadata" to (order.metadata ?: emptyMap<String, Any>()),
            "items" to order.items.map {
                mapOf(
                    "productId" to it.productId.toString(),
                    "productName" to it.productName,
                    "productSku" to it.productSku,
                    "productImage" to (it.productImage ?: ""),
                    "quantity" to it.quantity,
                    "unitPrice" to it.unitPrice,
                    "discount" to it.discount,
                    "attributes" to (it.attributes ?: emptyMap<String, Any>())
                )
            }
        )

        val event = OrderEvent(
            id = UUID.randomUUID(),
            orderId = orderId,
            eventType = OrderEventType.ORDER_CREATION_REQUESTED,
            payload = eventPayload,
            published = false
        )

        orderRepository.saveEvent(event)

        logger.info("[SERVICE] Order Request Event saved successfully for future Order ID: {}. Event ID: {}", orderId, event.id)
    }
}