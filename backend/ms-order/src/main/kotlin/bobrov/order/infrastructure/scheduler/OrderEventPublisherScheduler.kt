package bobrov.order.infrastructure.scheduler

import bobrov.order.core.domain.OrderCreatedEvent
import bobrov.order.core.ports.out.IOrderEventPublisherPort
import bobrov.order.core.ports.out.IOrderRepositoryPort
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Component
class OrderEventPublisherScheduler(
    private val orderRepository: IOrderRepositoryPort,
    private val orderEventPublisher: IOrderEventPublisherPort,
    private val objectMapper: ObjectMapper
) {

    private val logger = LoggerFactory.getLogger(OrderEventPublisherScheduler::class.java)

    @Scheduled(fixedDelay = 20000)
    @Transactional
    fun publishPendingEvents() {
        val pendingEvents = orderRepository.findUnpublishedEvents()
        
        if (pendingEvents.isNotEmpty()) {
            logger.info("[SCHEDULER] Found {} pending events to publish.", pendingEvents.size)
            
            pendingEvents.forEach { event ->
                try {
                    val orderCreatedEvent = objectMapper.convertValue(event.payload, OrderCreatedEvent::class.java)
                    orderEventPublisher.publish(orderCreatedEvent)
                    
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
