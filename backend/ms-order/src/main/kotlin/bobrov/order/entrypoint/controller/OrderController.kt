package bobrov.order.entrypoint.controller

import bobrov.order.core.ports.`in`.IOrderServicePort
import bobrov.order.entrypoint.dto.CreateOrderRequest
import bobrov.order.entrypoint.dto.OrderResponse
import bobrov.order.entrypoint.mapper.OrderEntrypointMapper
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/orders")
class OrderController(
    private val orderService: IOrderServicePort,
    private val orderEntrypointMapper: OrderEntrypointMapper
) {

    private val logger = LoggerFactory.getLogger(OrderController::class.java)

    @GetMapping
    fun getAllOrders(): List<OrderResponse> {
        logger.info("[CONTROLLER] Request received to list all orders.")
        val orders = orderService.getAllOrders().map { orderEntrypointMapper.toResponse(it) }
        logger.info("[CONTROLLER] Returning {} orders.", orders.size)
        return orders
    }

    @GetMapping("/{id}")
    fun getOrderById(@PathVariable id: UUID): OrderResponse {
        logger.info("[CONTROLLER] Request received to get order by ID: {}", id)
        val order = orderService.getOrderById(id)
        logger.info("[CONTROLLER] Order found: {}", order.orderNumber)
        return orderEntrypointMapper.toResponse(order)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createOrder(@RequestBody request: CreateOrderRequest) {
        logger.info("[CONTROLLER] Request received to create order for customer: {}", request.customerId)
        val orderDomain = orderEntrypointMapper.toDomain(request)
        orderService.createOrder(orderDomain)
        logger.info("[CONTROLLER] Order creation process initiated successfully.")
    }
}
