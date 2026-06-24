package bobrov.order.infrastructure.persistence.mapper

import bobrov.order.core.domain.Order
import bobrov.order.core.domain.OrderEvent
import bobrov.order.core.domain.OrderItem
import bobrov.order.core.domain.OrderStatus
import bobrov.order.core.domain.enums.ChangedBy
import bobrov.order.infrastructure.persistence.entity.OrderEntity
import bobrov.order.infrastructure.persistence.entity.OrderEventEntity
import bobrov.order.infrastructure.persistence.entity.OrderItemEntity
import bobrov.order.infrastructure.persistence.entity.OrderStatusEntity
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class OrderMapper {

    fun toDomain(entity: OrderEntity): Order {
        return Order(
            id = entity.id,
            orderNumber = entity.orderNumber,
            customerId = entity.customerId,
            subtotal = entity.subtotal,
            tax = entity.tax,
            shippingFee = entity.shippingFee,
            discount = entity.discount,
            totalAmount = entity.totalAmount,
            shippingAddress = entity.shippingAddress,
            paymentId = entity.paymentId,
            trackingCode = entity.trackingCode,
            notes = entity.notes,
            metadata = entity.metadata,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            version = entity.version,
            items = entity.items.map { toDomain(it) }.toMutableList(),
            statuses = entity.statuses.map { toDomain(it) }.toMutableList(),
            events = mutableListOf() // Eventos agora são desacoplados
        )
    }

    private fun toDomain(entity: OrderItemEntity): OrderItem {
        return OrderItem(
            id = entity.id,
            productId = entity.productId,
            productName = entity.productName,
            productSku = entity.productSku,
            productImage = entity.productImage,
            quantity = entity.quantity,
            unitPrice = entity.unitPrice,
            discount = entity.discount,
            subtotal = entity.subtotal,
            attributes = entity.attributes,
            createdAt = entity.createdAt
        )
    }

    private fun toDomain(entity: OrderStatusEntity): OrderStatus {
        val changedByEnum = try {
            entity.changedBy?.let { ChangedBy.valueOf(it) }
        } catch (e: IllegalArgumentException) {
            null
        }

        return OrderStatus(
            id = entity.id,
            statusType = entity.statusType,
            status = entity.status,
            isActive = entity.isActive,
            changedBy = changedByEnum,
            changedByUserId = if (changedByEnum == null) entity.changedBy else null,
            reason = entity.reason,
            metadata = entity.metadata,
            createdAt = entity.createdAt
        )
    }

    fun toDomain(entity: OrderEventEntity): OrderEvent {
        return OrderEvent(
            id = entity.id,
            orderId = entity.orderId,
            eventType = entity.eventType,
            aggregateType = entity.aggregateType,
            payload = entity.payload,
            published = entity.published,
            publishedAt = entity.publishedAt,
            snsMessageId = entity.snsMessageId,
            retryCount = entity.retryCount,
            lastError = entity.lastError,
            createdAt = entity.createdAt
        )
    }

    fun toEntity(domain: Order): OrderEntity {
        val entity = OrderEntity(
            id = domain.id,
            orderNumber = domain.orderNumber,
            customerId = domain.customerId,
            subtotal = domain.subtotal,
            tax = domain.tax,
            shippingFee = domain.shippingFee,
            discount = domain.discount,
            totalAmount = domain.totalAmount,
            shippingAddress = domain.shippingAddress,
            paymentId = domain.paymentId,
            trackingCode = domain.trackingCode,
            notes = domain.notes,
            metadata = domain.metadata,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt,
            version = domain.version
        )
        
        domain.items.forEach { 
            entity.items.add(toEntity(it, entity))
        }
        domain.statuses.forEach { 
            entity.statuses.add(toEntity(it, entity))
        }
        
        return entity
    }

    private fun toEntity(domain: OrderItem, orderEntity: OrderEntity): OrderItemEntity {
        return OrderItemEntity(
            id = domain.id,
            order = orderEntity,
            productId = domain.productId,
            productName = domain.productName,
            productSku = domain.productSku,
            productImage = domain.productImage,
            quantity = domain.quantity,
            unitPrice = domain.unitPrice,
            discount = domain.discount,
            subtotal = domain.subtotal,
            attributes = domain.attributes,
            createdAt = domain.createdAt
        )
    }

    private fun toEntity(domain: OrderStatus, orderEntity: OrderEntity): OrderStatusEntity {
        val changedByString = domain.changedBy?.name ?: domain.changedByUserId

        return OrderStatusEntity(
            id = domain.id,
            order = orderEntity,
            statusType = domain.statusType,
            status = domain.status,
            isActive = domain.isActive,
            changedBy = changedByString,
            reason = domain.reason,
            metadata = domain.metadata,
            createdAt = domain.createdAt
        )
    }

    fun toEntity(domain: OrderEvent): OrderEventEntity {
        return OrderEventEntity(
            id = domain.id,
            orderId = domain.orderId,
            eventType = domain.eventType,
            aggregateType = domain.aggregateType,
            payload = domain.payload,
            published = domain.published,
            publishedAt = domain.publishedAt,
            snsMessageId = domain.snsMessageId,
            retryCount = domain.retryCount,
            lastError = domain.lastError,
            createdAt = domain.createdAt
        )
    }
}
