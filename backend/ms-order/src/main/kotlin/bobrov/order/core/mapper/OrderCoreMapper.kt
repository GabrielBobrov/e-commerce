//package bobrov.order.core.mapper
//
//import bobrov.order.core.domain.Order
//import bobrov.order.core.domain.enums.OrderStatusType
//
//import org.springframework.stereotype.Component
//
//@Component
//class OrderCoreMapper {
//
//    fun toDto(domain: Order): OrderDto {
//        val currentStatus = domain.statuses.firstOrNull { it.isActive && it.statusType == OrderStatusType.ORDER }?.status?.name
//
//        return OrderDto(
//            id = domain.id!!,
//            orderNumber = domain.orderNumber,
//            customerId = domain.customerId,
//            subtotal = domain.subtotal,
//            tax = domain.tax,
//            shippingFee = domain.shippingFee,
//            discount = domain.discount,
//            totalAmount = domain.totalAmount,
//            shippingAddress = domain.shippingAddress,
//            status = currentStatus,
//            items = domain.items.map {
//                OrderItemDto(
//                    id = it.id!!,
//                    productId = it.productId,
//                    productName = it.productName,
//                    quantity = it.quantity,
//                    unitPrice = it.unitPrice,
//                    subtotal = it.subtotal
//                )
//            },
//            createdAt = domain.createdAt
//        )
//    }
//}
