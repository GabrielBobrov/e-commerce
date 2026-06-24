package bobrov.order.infrastructure.persistence

import bobrov.order.infrastructure.persistence.entity.OrderEventEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface OrderEventJpaRepository : JpaRepository<OrderEventEntity, UUID> {
    fun findTop10ByPublishedFalseOrderByCreatedAtAsc(): List<OrderEventEntity>
}
