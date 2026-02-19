package bobrov.order.entrypoint.controller

import bobrov.order.core.ports.`in`.IOrderServicePort
import bobrov.order.entrypoint.dto.CreateOrderRequest
import bobrov.order.entrypoint.dto.OrderResponse
import bobrov.order.entrypoint.mapper.OrderEntrypointMapper
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/orders")
class OrderController(
    private val orderService: IOrderServicePort,
    private val orderEntrypointMapper: OrderEntrypointMapper
) {

    @GetMapping
    fun getAllOrders(): List<OrderResponse> {
        return orderService.getAllOrders().map { orderEntrypointMapper.toResponse(it) }
    }

    @GetMapping("/{id}")
    fun getOrderById(@PathVariable id: UUID): OrderResponse {
        val order = orderService.getOrderById(id)
        return orderEntrypointMapper.toResponse(order)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createOrder(@RequestBody request: CreateOrderRequest) {
        val orderDomain = orderEntrypointMapper.toDomain(request)
        orderService.createOrder(orderDomain)
    }
}
