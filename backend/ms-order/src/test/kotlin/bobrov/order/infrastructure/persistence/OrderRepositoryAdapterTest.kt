package bobrov.order.infrastructure.persistence

import bobrov.order.core.domain.Order
import bobrov.order.infrastructure.persistence.cache.OrderCache
import bobrov.order.infrastructure.persistence.cache.OrderCacheMapper
import bobrov.order.infrastructure.persistence.entity.OrderEntity
import bobrov.order.infrastructure.persistence.mapper.OrderMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ValueOperations
import java.math.BigDecimal
import java.util.Optional
import java.util.UUID
import java.util.concurrent.TimeUnit

@ExtendWith(MockitoExtension::class)
class OrderRepositoryAdapterTest {

    @Mock
    private lateinit var orderJpaRepository: OrderJpaRepository

    @Mock
    private lateinit var orderEventJpaRepository: OrderEventJpaRepository

    @Mock
    private lateinit var redisTemplate: RedisTemplate<String, OrderCache>

    @Mock
    private lateinit var orderMapper: OrderMapper

    @Mock
    private lateinit var cacheMapper: OrderCacheMapper

    @Mock
    private lateinit var valueOperations: ValueOperations<String, OrderCache>

    @InjectMocks
    private lateinit var orderRepositoryAdapter: OrderRepositoryAdapter

    private val orderId = UUID.randomUUID()
    private val cacheKey = "order::$orderId"

    @Test
    fun `findById should return order from cache when cache hit`() {
        // Arrange
        val cachedOrder = OrderCache(id = orderId, orderNumber = "123", customerId = UUID.randomUUID(), totalAmount = 100.toBigDecimal(), status = null, items = emptyList(), createdAt = java.time.LocalDateTime.now(), updatedAt = java.time.LocalDateTime.now())
        val expectedOrder = Order(id = orderId, orderNumber = "123", customerId = UUID.randomUUID(), subtotal = 100.toBigDecimal(), totalAmount = 100.toBigDecimal(), shippingAddress = emptyMap(), items = mutableListOf())

        `when`(redisTemplate.opsForValue()).thenReturn(valueOperations)
        `when`(valueOperations.get(cacheKey)).thenReturn(cachedOrder)
        `when`(cacheMapper.toDomain(cachedOrder)).thenReturn(expectedOrder)

        // Act
        val result = orderRepositoryAdapter.findById(orderId)

        // Assert
        assertNotNull(result)
        assertEquals(expectedOrder, result)
        verify(orderJpaRepository, never()).findById(any()) // Verifica que o banco NÃO foi chamado
    }

    @Test
    fun `findById should return order from database and save to cache when cache miss`() {
        // Arrange
        val orderEntity = OrderEntity(id = orderId, orderNumber = "123", customerId = UUID.randomUUID(), subtotal = 100.toBigDecimal(), totalAmount = 100.toBigDecimal(), shippingAddress = emptyMap())
        val expectedOrder = Order(id = orderId, orderNumber = "123", customerId = UUID.randomUUID(), subtotal = 100.toBigDecimal(), totalAmount = 100.toBigDecimal(), shippingAddress = emptyMap(), items = mutableListOf())
        val orderToCache = OrderCache(id = orderId, orderNumber = "123", customerId = UUID.randomUUID(), totalAmount = 100.toBigDecimal(), status = null, items = emptyList(), createdAt = java.time.LocalDateTime.now(), updatedAt = java.time.LocalDateTime.now())

        // Simula cache miss
        `when`(redisTemplate.opsForValue()).thenReturn(valueOperations)
        `when`(valueOperations.get(cacheKey)).thenReturn(null)

        // Simula busca no banco
        `when`(orderJpaRepository.findById(orderId)).thenReturn(Optional.of(orderEntity))
        `when`(orderMapper.toDomain(orderEntity)).thenReturn(expectedOrder)

        // Simula mapeamento para o cache
        `when`(cacheMapper.toCache(expectedOrder)).thenReturn(orderToCache)

        // Act
        val result = orderRepositoryAdapter.findById(orderId)

        // Assert
        assertNotNull(result)
        assertEquals(expectedOrder, result)
        verify(orderJpaRepository, times(1)).findById(orderId) // Verifica que o banco foi chamado 1 vez
        verify(valueOperations, times(1)).set(cacheKey, orderToCache, 10, TimeUnit.MINUTES) // Verifica que o cache foi salvo
    }

    // Helper para o 'any()' do Mockito funcionar com Kotlin non-null types
    private fun <T> any(): T {
        org.mockito.Mockito.any<T>()
        return uninitialized()
    }

    private fun <T> uninitialized(): T = null as T
}