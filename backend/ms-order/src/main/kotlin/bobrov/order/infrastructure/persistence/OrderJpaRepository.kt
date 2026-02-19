package bobrov.order.infrastructure.persistence

import bobrov.order.infrastructure.persistence.entity.OrderEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface OrderJpaRepository : JpaRepository<OrderEntity, UUID> {
    fun findByOrderNumber(orderNumber: String): OrderEntity?
    fun findByCustomerId(customerId: UUID): List<OrderEntity>
}
