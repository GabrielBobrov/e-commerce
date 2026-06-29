package bobrov.order.infrastructure.persistence

import bobrov.order.core.domain.Order
import bobrov.order.core.domain.OrderEvent
import bobrov.order.core.ports.out.IOrderRepositoryPort
import bobrov.order.infrastructure.persistence.cache.OrderCache
import bobrov.order.infrastructure.persistence.cache.OrderCacheMapper
import bobrov.order.infrastructure.persistence.mapper.OrderMapper
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.util.UUID
import java.util.concurrent.TimeUnit

@Component
class OrderRepositoryAdapter(
    private val orderJpaRepository: OrderJpaRepository,
    private val orderEventJpaRepository: OrderEventJpaRepository,
    private val orderMapper: OrderMapper,
    private val redisTemplate: RedisTemplate<String, OrderCache>,
    private val cacheMapper: OrderCacheMapper
) : IOrderRepositoryPort {

    private val logger = LoggerFactory.getLogger(OrderRepositoryAdapter::class.java)
    private val cacheKeyPrefix = "order::"

    override fun save(order: Order): Order {
        logger.debug("[REPOSITORY] Saving order: {}", order.orderNumber)
        val entity = orderMapper.toEntity(order)
        val savedEntity = orderJpaRepository.save(entity)
        logger.debug("[REPOSITORY] Order saved with ID: {}", savedEntity.id)

        // Invalida o cache para garantir consistência
        savedEntity.id?.let {
            val cacheKey = cacheKeyPrefix + it
            redisTemplate.delete(cacheKey)
            logger.debug("[CACHE] Invalidated cache for key: {}", cacheKey)
        }

        return orderMapper.toDomain(savedEntity)
    }

    override fun findById(id: UUID): Order? {
        val cacheKey = cacheKeyPrefix + id
        logger.debug("[CACHE] Searching for order in cache with key: {}", cacheKey)

        // 1. Tenta buscar do cache
        val cachedOrder = redisTemplate.opsForValue().get(cacheKey)
        if (cachedOrder != null) {
            logger.info("[CACHE] HIT! Found order in cache. Key: {}", cacheKey)
            return cacheMapper.toDomain(cachedOrder)
        }

        logger.warn("[CACHE] MISS! Order not found in cache. Key: {}", cacheKey)

        // 2. Se não encontrar, busca no banco
        val orderFromDb = orderJpaRepository.findById(id)
            .map { orderMapper.toDomain(it) }
            .orElse(null)

        // 3. Se encontrar no banco, converte para o modelo de cache e salva no Redis
        orderFromDb?.let {
            logger.debug("[CACHE] Storing order in cache. Key: {}", cacheKey)
            val orderToCache = cacheMapper.toCache(it)
            redisTemplate.opsForValue().set(cacheKey, orderToCache, 10, TimeUnit.MINUTES)
        }

        return orderFromDb
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