package bobrov.order.core.ports.out

import bobrov.order.core.domain.OrderCreatedEvent

interface IOrderEventPublisherPort {
    fun publish(event: OrderCreatedEvent)
}
