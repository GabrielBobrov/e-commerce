package bobrov.order.infrastructure.messaging

import bobrov.order.core.domain.OrderCreatedEvent
import bobrov.order.core.ports.out.IOrderEventPublisherPort
import io.awspring.cloud.sns.core.SnsTemplate
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class SnsOrderEventPublisherAdapter(
    private val snsTemplate: SnsTemplate,
    @Value("\${aws.sns.topic.order-events}")
    private val topicArn: String
) : IOrderEventPublisherPort {

    private val logger = LoggerFactory.getLogger(SnsOrderEventPublisherAdapter::class.java)

    override fun publish(event: OrderCreatedEvent) {
        logger.info("[PUBLISHER] Publicando evento no SNS. Topic: {}, OrderNumber: {}", topicArn, event.orderNumber)
        try {
            snsTemplate.convertAndSend(topicArn, event)
            logger.info("[PUBLISHER] Evento enviado com sucesso.")
        } catch (e: Exception) {
            logger.error("[PUBLISHER] Erro ao publicar evento no SNS: {}", e.message, e)
            throw e
        }
    }
}
