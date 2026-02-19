package bobrov.order.core.adapter.service

import bobrov.order.core.domain.Order
import bobrov.order.core.domain.OrderEvent
import bobrov.order.core.domain.OrderStatus
import bobrov.order.core.ports.`in`.IOrderServicePort
import bobrov.order.core.ports.out.IOrderRepositoryPort
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
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
        logger.info("[SERVICE] Iniciando criação de pedido para cliente: {}", order.customerId)

        // Lógica simplificada de cálculo
        val subtotal = order.items.sumOf { it.unitPrice.multiply(BigDecimal(it.quantity)) }
        val totalAmount = subtotal // + tax + shipping - discount

        // Atualiza os itens com o subtotal calculado
        val enrichedItems = order.items.map { item ->
            item.copy(
                subtotal = item.unitPrice.multiply(BigDecimal(item.quantity)).subtract(item.discount)
            )
        }.toMutableList()

        // Atualiza o objeto de domínio com os valores calculados
        val enrichedOrder = order.copy(
            orderNumber = UUID.randomUUID().toString().substring(0, 8).uppercase(), // Exemplo simples
            subtotal = subtotal,
            totalAmount = totalAmount,
            items = enrichedItems
        )
        
        val initialStatus = OrderStatus(
            statusType = "ORDER",
            status = "PENDING",
            isActive = true,
            changedBy = "SYSTEM",
            reason = "Order created"
        )

        enrichedOrder.statuses.add(initialStatus)

        // Cria o evento de domínio (Outbox) com payload completo
        val eventPayload = mapOf(
            "orderNumber" to enrichedOrder.orderNumber,
            "customerId" to enrichedOrder.customerId.toString(),
            "totalAmount" to enrichedOrder.totalAmount.toString(),
            "shippingAddress" to enrichedOrder.shippingAddress,
            "items" to enrichedOrder.items.map { 
                mapOf(
                    "productId" to it.productId.toString(),
                    "productName" to it.productName,
                    "quantity" to it.quantity,
                    "unitPrice" to it.unitPrice
                )
            }
        )

        val event = OrderEvent(
            eventType = "ORDER_CREATED",
            payload = eventPayload,
            published = false
        )
        
        enrichedOrder.events.add(event)

        val savedOrder = orderRepository.save(enrichedOrder)
        logger.info("[SERVICE] Pedido e Evento salvos no banco com sucesso. ID: {}", savedOrder.id)
    }
}
