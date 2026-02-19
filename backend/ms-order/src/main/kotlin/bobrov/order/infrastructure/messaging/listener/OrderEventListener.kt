package bobrov.order.infrastructure.messaging.listener

import bobrov.order.core.domain.Order
import bobrov.order.core.domain.OrderCreatedEvent
import bobrov.order.core.domain.OrderItem
import bobrov.order.core.domain.OrderStatus
import bobrov.order.core.ports.out.IOrderRepositoryPort
import com.fasterxml.jackson.databind.ObjectMapper
import io.awspring.cloud.sqs.annotation.SqsListener
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Component
class OrderEventListener(
    private val orderRepository: IOrderRepositoryPort,
    private val objectMapper: ObjectMapper
) {

    private val logger = LoggerFactory.getLogger(OrderEventListener::class.java)

    @SqsListener("order-events-queue")
    @Transactional
    fun receiveOrderEvent(message: String) {
        logger.info("[LISTENER] Mensagem recebida da fila SQS.")
        
        try {
            // Parse do envelope SNS
            val snsEnvelope = objectMapper.readTree(message)
            val snsMessage = snsEnvelope.get("Message").asText()
            
            logger.debug("[LISTENER] Payload SNS extraído: {}", snsMessage)
            
            // Parse do payload real (OrderCreatedEvent)
            val event = objectMapper.readValue(snsMessage, OrderCreatedEvent::class.java)
            logger.info("[LISTENER] Evento OrderCreatedEvent desserializado. OrderNumber: {}", event.orderNumber)

            // Verifica se o pedido já existe (Idempotência)
            val existingOrder = orderRepository.findByOrderNumber(event.orderNumber)
            if (existingOrder != null) {
                logger.warn("[LISTENER] Pedido {} já existe no banco. Ignorando processamento duplicado.", event.orderNumber)
                return
            }

            // Lógica simplificada de cálculo
            val subtotal = event.items.sumOf { it.unitPrice.multiply(BigDecimal(it.quantity)) }
            val totalAmount = event.totalAmount ?: subtotal

            val order = Order(
                orderNumber = event.orderNumber,
                customerId = event.customerId,
                subtotal = subtotal,
                totalAmount = totalAmount,
                shippingAddress = event.shippingAddress,
                notes = event.notes,
                metadata = event.metadata
            )

            val items = event.items.map { itemEvent ->
                OrderItem(
                    productId = itemEvent.productId,
                    productName = itemEvent.productName,
                    productSku = itemEvent.productSku,
                    productImage = itemEvent.productImage,
                    quantity = itemEvent.quantity,
                    unitPrice = itemEvent.unitPrice,
                    discount = itemEvent.discount,
                    subtotal = itemEvent.unitPrice.multiply(BigDecimal(itemEvent.quantity)).subtract(itemEvent.discount),
                    attributes = itemEvent.attributes
                )
            }
            
            val initialStatus = OrderStatus(
                statusType = "ORDER",
                status = "PENDING",
                isActive = true,
                changedBy = "SYSTEM",
                reason = "Order created via SQS"
            )

            order.items.addAll(items)
            order.statuses.add(initialStatus)

            val savedOrder = orderRepository.save(order)
            logger.info("[LISTENER] Pedido salvo no banco com sucesso. ID: {}", savedOrder.id)
            
        } catch (e: Exception) {
            logger.error("[LISTENER] Erro ao processar mensagem SQS: {}", e.message, e)
            // Não relança a exceção para evitar loop infinito no SQS se for erro de negócio
            // Em produção, deveria ir para DLQ
        }
    }
}
