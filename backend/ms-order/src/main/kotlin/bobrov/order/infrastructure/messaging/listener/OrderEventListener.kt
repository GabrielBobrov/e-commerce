package bobrov.order.infrastructure.messaging.listener

import bobrov.order.core.domain.Order
import bobrov.order.core.domain.OrderCreatedEvent
import bobrov.order.core.domain.OrderItem
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
        logger.info("[LISTENER] Message received from SQS queue.")
        
        try {
            val snsEnvelope = objectMapper.readTree(message)
            val snsMessage = snsEnvelope.get("Message").asText()
            
            logger.debug("[LISTENER] Extracted SNS payload: {}", snsMessage)
            
            val event = objectMapper.readValue(snsMessage, OrderCreatedEvent::class.java)
            logger.info("[LISTENER] Order Request Event deserialized. Customer: {}", event.customerId)

            val orderNumberToCheck = event.orderNumber ?: ""
            
            if (orderNumberToCheck.isNotBlank()) {
                val existingOrder = orderRepository.findByOrderNumber(orderNumberToCheck)
                if (existingOrder != null) {
                    logger.warn("[LISTENER] Order {} already exists. Ignoring.", orderNumberToCheck)
                    return
                }
            }

            val rawOrder = Order(
                orderNumber = event.orderNumber ?: "",
                customerId = event.customerId,
                shippingAddress = event.shippingAddress,
                notes = event.notes,
                metadata = event.metadata,
                items = event.items.map { itemEvent ->
                    OrderItem(
                        productId = itemEvent.productId,
                        productName = itemEvent.productName,
                        productSku = itemEvent.productSku,
                        productImage = itemEvent.productImage,
                        quantity = itemEvent.quantity,
                        unitPrice = itemEvent.unitPrice,
                        discount = itemEvent.discount
                    )
                }.toMutableList()
            )

            val initializedOrder = rawOrder.initializeOrder()

            val savedOrder = orderRepository.save(initializedOrder)
            logger.info("[LISTENER] Order processed and saved successfully. OrderNumber: {}, ID: {}", savedOrder.orderNumber, savedOrder.id)
            
        } catch (e: Exception) {
            logger.error("[LISTENER] Error processing SQS message: {}", e.message, e)
        }
    }
}
