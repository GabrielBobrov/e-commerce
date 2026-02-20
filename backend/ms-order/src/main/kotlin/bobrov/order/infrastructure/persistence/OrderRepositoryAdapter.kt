package bobrov.order.infrastructure.persistence

import bobrov.order.core.domain.Order
import bobrov.order.core.domain.OrderEvent
import bobrov.order.core.ports.out.IOrderRepositoryPort
import bobrov.order.infrastructure.persistence.mapper.OrderMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class OrderRepositoryAdapter(
    private val orderJpaRepository: OrderJpaRepository,
    private val orderEventJpaRepository: OrderEventJpaRepository,
    private val orderMapper: OrderMapper
) : IOrderRepositoryPort {

    private val logger = LoggerFactory.getLogger(OrderRepositoryAdapter::class.java)

    override fun save(order: Order): Order {
        logger.debug("[REPOSITORY] Saving order: {}", order.orderNumber)
        val entity = orderMapper.toEntity(order)
        val savedEntity = orderJpaRepository.save(entity)
        logger.debug("[REPOSITORY] Order saved with ID: {}", savedEntity.id)
        return orderMapper.toDomain(savedEntity)
    }

    override fun findById(id: UUID): Order? {
        logger.debug("[REPOSITORY] Finding order by ID: {}", id)
        return orderJpaRepository.findById(id)
            .map { orderMapper.toDomain(it) }
            .orElse(null)
    }

    override fun findAll(): List<Order> {
        logger.debug("[REPOSITORY] Finding all orders")
        return orderJpaRepository.findAll()
            .map { orderMapper.toDomain(it) }
    }

    override fun findByOrderNumber(orderNumber: String): Order? {
        logger.debug("[REPOSITORY] Finding order by number: {}", orderNumber)
        return orderJpaRepository.findByOrderNumber(orderNumber)
            ?.let { orderMapper.toDomain(it) }
    }

    override fun findByCustomerId(customerId: UUID): List<Order> {
        logger.debug("[REPOSITORY] Finding orders by customer ID: {}", customerId)
        return orderJpaRepository.findByCustomerId(customerId)
            .map { orderMapper.toDomain(it) }
    }

    override fun saveEvent(event: OrderEvent): OrderEvent {
        logger.debug("[REPOSITORY] Saving event. ID: {}, Published: {}", event.id, event.published)
        
        val entity = orderMapper.toEntity(event)
        val savedEntity = orderEventJpaRepository.save(entity)
        
        return orderMapper.toDomain(savedEntity)
    }

    override fun findUnpublishedEvents(): List<OrderEvent> {
        logger.debug("[REPOSITORY] Finding unpublished events")
        return orderEventJpaRepository.findTop10ByPublishedFalseOrderByCreatedAtAsc()
            .map { orderMapper.toDomain(it) }
    }
}
