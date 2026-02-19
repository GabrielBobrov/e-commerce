package bobrov.order.entrypoint.mapper

import bobrov.order.core.domain.Order
import bobrov.order.core.domain.OrderItem
import bobrov.order.entrypoint.dto.CreateOrderRequest
import bobrov.order.entrypoint.dto.OrderItemResponse
import bobrov.order.entrypoint.dto.OrderResponse
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class OrderEntrypointMapper {

    fun toDomain(request: CreateOrderRequest): Order {
        val items = request.items.map { 
            OrderItem(
                productId = it.productId,
                productName = it.productName,
                productSku = it.productSku,
                productImage = it.productImage,
                quantity = it.quantity,
                unitPrice = it.unitPrice,
                discount = it.discount ?: BigDecimal.ZERO,
                subtotal = BigDecimal.ZERO // Será calculado no serviço
            )
        }.toMutableList()

        return Order(
            orderNumber = "", // Será gerado no serviço
            customerId = request.customerId,
            subtotal = BigDecimal.ZERO, // Será calculado no serviço
            totalAmount = BigDecimal.ZERO, // Será calculado no serviço
            shippingAddress = request.shippingAddress,
            notes = request.notes,
            metadata = request.metadata,
            items = items
        )
    }

    fun toResponse(domain: Order): OrderResponse {
        val currentStatus = domain.statuses.firstOrNull { it.isActive && it.statusType == "ORDER" }?.status
        
        return OrderResponse(
            id = domain.id!!,
            orderNumber = domain.orderNumber,
            customerId = domain.customerId,
            subtotal = domain.subtotal,
            tax = domain.tax,
            shippingFee = domain.shippingFee,
            discount = domain.discount,
            totalAmount = domain.totalAmount,
            shippingAddress = domain.shippingAddress,
            status = currentStatus,
            items = domain.items.map { toResponse(it) },
            createdAt = domain.createdAt
        )
    }

    private fun toResponse(domain: OrderItem): OrderItemResponse {
        return OrderItemResponse(
            id = domain.id!!,
            productId = domain.productId,
            productName = domain.productName,
            quantity = domain.quantity,
            unitPrice = domain.unitPrice,
            subtotal = domain.subtotal
        )
    }
}
