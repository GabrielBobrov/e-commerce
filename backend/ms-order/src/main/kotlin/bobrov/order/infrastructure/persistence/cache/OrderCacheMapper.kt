package bobrov.order.infrastructure.persistence.cache

import bobrov.order.core.domain.Order
import bobrov.order.core.domain.OrderItem
import bobrov.order.core.domain.enums.OrderState
import bobrov.order.core.domain.enums.OrderStatusType
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class OrderCacheMapper {

    fun toCache(domain: Order): OrderCache {
        val currentDomainStatus = domain.statuses.firstOrNull { it.isActive && it.statusType == OrderStatusType.ORDER }?.status
        return OrderCache(
            id = domain.id!!,
            orderNumber = domain.orderNumber,
            customerId = domain.customerId,
            totalAmount = domain.totalAmount,
            status = currentDomainStatus?.let { OrderCacheStatus.valueOf(it.name) },
            items = domain.items.map { toCacheItem(it) },
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt
        )
    }

    fun toDomain(cache: OrderCache): Order {
        return Order(
            id = cache.id,
            orderNumber = cache.orderNumber,
            customerId = cache.customerId,
            totalAmount = cache.totalAmount,
            items = cache.items.map { toDomainItem(it) }.toMutableList(),
            createdAt = cache.createdAt,
            updatedAt = cache.updatedAt,
            subtotal = BigDecimal.ZERO,
            shippingAddress = emptyMap()
        )
    }

    private fun toCacheItem(item: OrderItem): OrderItemCache {
        return OrderItemCache(
            id = item.id!!,
            productId = item.productId,
            productName = item.productName,
            productSku = item.productSku,
            quantity = item.quantity,
            unitPrice = item.unitPrice,
            subtotal = item.subtotal
        )
    }

    private fun toDomainItem(item: OrderItemCache): OrderItem {
        return OrderItem(
            id = item.id,
            productId = item.productId,
            productName = item.productName,
            productSku = item.productSku,
            quantity = item.quantity,
            unitPrice = item.unitPrice,
            subtotal = item.subtotal
        )
    }
}