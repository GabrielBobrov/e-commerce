package bobrov.order.infrastructure.scheduler

import bobrov.order.core.domain.OrderCreatedEvent
import bobrov.order.core.domain.OrderItemEvent
import bobrov.order.core.ports.out.IOrderEventPublisherPort
import bobrov.order.core.ports.out.IOrderRepositoryPort
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Component
class OrderEventPublisherScheduler(
    private val orderRepository: IOrderRepositoryPort,
    private val orderEventPublisher: IOrderEventPublisherPort
) {

    private val logger = LoggerFactory.getLogger(OrderEventPublisherScheduler::class.java)

    @Scheduled(fixedDelay = 20000) // Roda a cada 20 segundos
    @Transactional
    fun publishPendingEvents() {
        val pendingEvents = orderRepository.findUnpublishedEvents()
        
        if (pendingEvents.isNotEmpty()) {
            logger.info("[SCHEDULER] Found {} pending events to publish.", pendingEvents.size)
            
            pendingEvents.forEach { event ->
                try {
                    val payload = event.payload
                    
                    // Reconstrução do evento a partir do payload salvo
                    val orderCreatedEvent = OrderCreatedEvent(
                        orderNumber = payload["orderNumber"] as String,
                        customerId = UUID.fromString(payload["customerId"] as String),
                        items = (payload["items"] as List<Map<String, Any>>).map { item ->
                            OrderItemEvent(
                                productId = UUID.fromString(item["productId"] as String),
                                productName = item["productName"] as String,
                                productSku = "", // Simplificação
                                quantity = item["quantity"] as Int,
                                unitPrice = BigDecimal(item["unitPrice"].toString())
                            )
                        },
                        shippingAddress = payload["shippingAddress"] as Map<String, Any>,
                        totalAmount = BigDecimal(payload["totalAmount"].toString())
                    )
                    
                    orderEventPublisher.publish(orderCreatedEvent)
                    
                    // Atualiza o status do evento
                    event.published = true
                    event.publishedAt = LocalDateTime.now()
                    orderRepository.saveEvent(event)
                    
                    logger.info("[SCHEDULER] Event {} published successfully.", event.id)
                } catch (e: Exception) {
                    logger.error("[SCHEDULER] Error publishing event {}: {}", event.id, e.message)
                    event.retryCount++
                    event.lastError = e.message
                    orderRepository.saveEvent(event)
                }
            }
        }
    }
}
