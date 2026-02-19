package bobrov.order.entrypoint.exception.model

import com.fasterxml.jackson.annotation.JsonInclude
import java.time.OffsetDateTime

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Problem(
    val status: Int? = null,
    val timestamp: OffsetDateTime? = null,
    val type: String? = null,
    val title: String? = null,
    val detail: String? = null,
    val userMessage: String? = null,
    val objects: List<Object>? = null
) {
    data class Object(
        val name: String,
        val userMessage: String
    )

    class Builder {
        private var status: Int? = null
        private var timestamp: OffsetDateTime? = null
        private var type: String? = null
        private var title: String? = null
        private var detail: String? = null
        private var userMessage: String? = null
        private var objects: List<Object>? = null

        fun status(status: Int?) = apply { this.status = status }
        fun timestamp(timestamp: OffsetDateTime?) = apply { this.timestamp = timestamp }
        fun type(type: String?) = apply { this.type = type }
        fun title(title: String?) = apply { this.title = title }
        fun detail(detail: String?) = apply { this.detail = detail }
        fun userMessage(userMessage: String?) = apply { this.userMessage = userMessage }
        fun objects(objects: List<Object>?) = apply { this.objects = objects }

        fun build() = Problem(status, timestamp, type, title, detail, userMessage, objects)
    }

    companion object {
        fun builder() = Builder()
    }
}
