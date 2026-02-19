package bobrov.order.entrypoint.exception.model.enums

enum class ProblemType(val uri: String, val title: String) {
    INVALID_DATA("/invalid-data", "Dados inválidos"),
    SYSTEM_ERROR("/system-error", "Erro de sistema"),
    INVALID_PARAMETER("/invalid-parameter", "Parâmetro inválido"),
    MESSAGE_INCOMPRESSIBLE("/message-incompressible", "Mensagem incompreensível"),
    RESOURCE_NOT_FOUND("/resource-not-found", "Recurso não encontrado"),
    BUSINESS_ERROR("/business-error", "Violação de regra de negócio");
}
