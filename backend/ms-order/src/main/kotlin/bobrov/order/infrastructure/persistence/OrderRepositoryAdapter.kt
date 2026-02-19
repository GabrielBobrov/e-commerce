package bobrov.order.infrastructure.persistence

import bobrov.order.core.domain.Order
import bobrov.order.core.domain.OrderEvent
import bobrov.order.core.ports.out.IOrderRepositoryPort
import bobrov.order.infrastructure.persistence.mapper.OrderMapper
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class OrderRepositoryAdapter(
    private val orderJpaRepository: OrderJpaRepository,
    private val orderEventJpaRepository: OrderEventJpaRepository,
    private val orderMapper: OrderMapper
) : IOrderRepositoryPort {

    override fun save(order: Order): Order {
        val entity = orderMapper.toEntity(order)
        val savedEntity = orderJpaRepository.save(entity)
        return orderMapper.toDomain(savedEntity)
    }

    override fun findById(id: UUID): Order? {
        return orderJpaRepository.findById(id)
            .map { orderMapper.toDomain(it) }
            .orElse(null)
    }

    override fun findAll(): List<Order> {
        return orderJpaRepository.findAll()
            .map { orderMapper.toDomain(it) }
    }

    override fun findByOrderNumber(orderNumber: String): Order? {
        return orderJpaRepository.findByOrderNumber(orderNumber)
            ?.let { orderMapper.toDomain(it) }
    }

    override fun findByCustomerId(customerId: UUID): List<Order> {
        return orderJpaRepository.findByCustomerId(customerId)
            .map { orderMapper.toDomain(it) }
    }

    override fun saveEvent(event: OrderEvent): OrderEvent {
        // Como o evento está vinculado a um pedido, precisamos garantir que o pedido exista
        // Mas aqui vamos assumir que o evento já tem o ID do pedido ou que o pedido será salvo junto
        // Para simplificar, vamos salvar o evento diretamente se ele tiver o pedido vinculado
        // Mas o ideal é salvar o pedido com os eventos na lista dele
        
        // Na verdade, o save(order) já salva os eventos se estiverem na lista do pedido (CascadeType.ALL)
        // Então esse método pode ser usado para atualizar o status de publicação
        
        // Vamos implementar buscando o evento pelo ID e atualizando
        if (event.id != null) {
            val entity = orderEventJpaRepository.findById(event.id).orElseThrow()
            entity.published = event.published
            entity.publishedAt = event.publishedAt
            entity.snsMessageId = event.snsMessageId
            entity.retryCount = event.retryCount
            entity.lastError = event.lastError
            
            val saved = orderEventJpaRepository.save(entity)
            return orderMapper.toDomain(saved)
        }
        return event
    }

    override fun findUnpublishedEvents(): List<OrderEvent> {
        return orderEventJpaRepository.findTop10ByPublishedFalseOrderByCreatedAtAsc()
            .map { orderMapper.toDomain(it) }
    }
}
