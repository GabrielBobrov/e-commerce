package bobrov.order.entrypoint.exception

import bobrov.order.core.exception.BusinessException
import bobrov.order.core.exception.NotFoundException
import bobrov.order.core.exception.OrderAlreadyExistsException
import bobrov.order.entrypoint.exception.model.Problem
import bobrov.order.entrypoint.exception.model.enums.ProblemType
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.exc.InvalidFormatException
import com.fasterxml.jackson.databind.exc.PropertyBindingException
import org.apache.commons.lang3.exception.ExceptionUtils
import org.springframework.beans.TypeMismatchException
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.dao.InvalidDataAccessApiUsageException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.BindingResult
import org.springframework.validation.FieldError
import org.springframework.web.HttpMediaTypeNotAcceptableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.NoHandlerFoundException
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.time.OffsetDateTime
import java.util.NoSuchElementException
import java.util.stream.Collectors

@ControllerAdvice
class GlobalExceptionHandler(
    private val messageSource: MessageSource
) : ResponseEntityExceptionHandler() {

    companion object {
        const val MSG_ERRO_GENERICA_USUARIO_FINAL =
            "Ocorreu um erro interno inesperado no sistema. Tente novamente e se " +
                    "o problema persistir, entre em contato com o administrador do sistema."
    }

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFound(ex: RuntimeException, request: WebRequest): ResponseEntity<Any>? {
        val status = HttpStatus.NOT_FOUND
        val problemType = ProblemType.RESOURCE_NOT_FOUND
        val detail = ex.message

        val problem = createProblemBuilder(status, problemType, detail)
            .userMessage(detail)
            .build()

        return handleExceptionInternal(ex, problem, HttpHeaders(), status, request)
    }

    @ExceptionHandler(OrderAlreadyExistsException::class)
    fun handleOrderAlreadyExists(ex: RuntimeException, request: WebRequest): ResponseEntity<Any>? {
        val status = HttpStatus.CONFLICT
        val problemType = ProblemType.BUSINESS_ERROR
        val detail = ex.message

        val problem = createProblemBuilder(status, problemType, detail)
            .userMessage(detail)
            .build()

        return handleExceptionInternal(ex, problem, HttpHeaders(), status, request)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException, request: WebRequest): ResponseEntity<Any>? {
        val status = HttpStatus.BAD_REQUEST
        val problemType = ProblemType.INVALID_DATA
        val detail = ex.message

        val problem = createProblemBuilder(status, problemType, detail)
            .userMessage(detail)
            .build()

        return handleExceptionInternal(ex, problem, HttpHeaders(), status, request)
    }

    override fun handleMissingServletRequestParameter(
        ex: MissingServletRequestParameterException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any>? {
        val problemType = ProblemType.INVALID_PARAMETER
        val detail = "O parâmetro de URL '${ex.parameterName}' é obrigatório"

        val problem = createProblemBuilder(status, problemType, detail)
            .userMessage(detail)
            .build()

        return handleExceptionInternal(ex, problem, headers, status, request)
    }

    @ExceptionHandler(InvalidDataAccessApiUsageException::class)
    fun handleInvalidDataAccessApiUsage(
        ex: InvalidDataAccessApiUsageException,
        request: WebRequest
    ): ResponseEntity<Any>? {
        val status = HttpStatus.BAD_REQUEST
        val problemType = ProblemType.INVALID_DATA
        val detail = ex.message

        val problem = createProblemBuilder(status, problemType, detail)
            .userMessage(detail)
            .build()

        return handleExceptionInternal(ex, problem, HttpHeaders(), status, request)
    }

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNoSuchElement(ex: NoSuchElementException, request: WebRequest): ResponseEntity<Any>? {
        val status = HttpStatus.BAD_REQUEST
        val problemType = ProblemType.BUSINESS_ERROR
        val detail = ex.message

        val problem = createProblemBuilder(status, problemType, detail)
            .userMessage(detail)
            .build()

        return handleExceptionInternal(ex, problem, HttpHeaders(), status, request)
    }

    override fun handleHttpMediaTypeNotAcceptable(
        ex: HttpMediaTypeNotAcceptableException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any>? {
        return ResponseEntity.status(status).headers(headers).build()
    }

    override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any>? {
        return handleValidationInternal(ex, headers, status, request, ex.bindingResult)
    }

    private fun handleValidationInternal(
        ex: Exception,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest,
        bindingResult: BindingResult
    ): ResponseEntity<Any>? {
        val problemType = ProblemType.INVALID_DATA
        val detail = "Um ou mais campos estão inválidos. Faça o preenchimento correto e tente novamente."

        val problemObjects = bindingResult.allErrors.map { objectError ->
            val message = messageSource.getMessage(objectError, LocaleContextHolder.getLocale())
            var name = objectError.objectName

            if (objectError is FieldError) {
                name = objectError.field
            }

            Problem.Object(name, message)
        }

        val problem = createProblemBuilder(status, problemType, detail)
            .userMessage(detail)
            .objects(problemObjects)
            .build()

        return handleExceptionInternal(ex, problem, headers, status, request)
    }

    @ExceptionHandler(Exception::class)
    fun handleUncaught(ex: Exception, request: WebRequest): ResponseEntity<Any>? {
        val status = HttpStatus.INTERNAL_SERVER_ERROR
        val problemType = ProblemType.SYSTEM_ERROR
        val detail = MSG_ERRO_GENERICA_USUARIO_FINAL

        ex.printStackTrace()

        val problem = createProblemBuilder(status, problemType, detail)
            .userMessage(detail)
            .build()

        return handleExceptionInternal(ex, problem, HttpHeaders(), status, request)
    }

    override fun handleNoHandlerFoundException(
        ex: NoHandlerFoundException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any>? {
        val problemType = ProblemType.RESOURCE_NOT_FOUND
        val detail = "O recurso ${ex.requestURL}, que você tentou acessar, é inexistente."

        val problem = createProblemBuilder(status, problemType, detail)
            .userMessage(MSG_ERRO_GENERICA_USUARIO_FINAL)
            .build()

        return handleExceptionInternal(ex, problem, headers, status, request)
    }

    override fun handleTypeMismatch(
        ex: TypeMismatchException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any>? {
        if (ex is MethodArgumentTypeMismatchException) {
            return handleMethodArgumentTypeMismatch(ex, headers, status, request)
        }
        return super.handleTypeMismatch(ex, headers, status, request)
    }

    private fun handleMethodArgumentTypeMismatch(
        ex: MethodArgumentTypeMismatchException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any>? {
        val problemType = ProblemType.INVALID_PARAMETER
        val detail = "O parâmetro de URL '${ex.name}' recebeu o valor '${ex.value}', " +
                "que é de um tipo inválido. Corrija e informe um valor compatível com o tipo ${ex.requiredType?.simpleName}."

        val problem = createProblemBuilder(status, problemType, detail)
            .userMessage(MSG_ERRO_GENERICA_USUARIO_FINAL)
            .build()

        return handleExceptionInternal(ex, problem, headers, status, request)
    }

    override fun handleHttpMessageNotReadable(
        ex: HttpMessageNotReadableException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any>? {
        val rootCause = ExceptionUtils.getRootCause(ex)

        if (rootCause is InvalidFormatException) {
            return handleInvalidFormat(rootCause, headers, status, request)
        } else if (rootCause is PropertyBindingException) {
            return handlePropertyBinding(rootCause, headers, status, request)
        }

        val problemType = ProblemType.MESSAGE_INCOMPRESSIBLE
        val detail = "O corpo da requisição está inválido. Verifique erro de sintaxe."

        val problem = createProblemBuilder(status, problemType, detail)
            .userMessage(MSG_ERRO_GENERICA_USUARIO_FINAL)
            .build()

        return handleExceptionInternal(ex, problem, headers, status, request)
    }

    private fun handlePropertyBinding(
        ex: PropertyBindingException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any>? {
        val path = joinPath(ex.path)
        val problemType = ProblemType.MESSAGE_INCOMPRESSIBLE
        val detail = "A propriedade '$path' não existe. " +
                "Corrija ou remova essa propriedade e tente novamente."

        val problem = createProblemBuilder(status, problemType, detail)
            .userMessage(MSG_ERRO_GENERICA_USUARIO_FINAL)
            .build()

        return handleExceptionInternal(ex, problem, headers, status, request)
    }

    private fun handleInvalidFormat(
        ex: InvalidFormatException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any>? {
        val path = joinPath(ex.path)
        val problemType = ProblemType.MESSAGE_INCOMPRESSIBLE
        val detail = "A propriedade '$path' recebeu o valor '${ex.value}', " +
                "que é de um tipo inválido. Corrija e informe um valor compatível com o tipo ${ex.targetType.simpleName}."

        val problem = createProblemBuilder(status, problemType, detail)
            .userMessage(MSG_ERRO_GENERICA_USUARIO_FINAL)
            .build()

        return handleExceptionInternal(ex, problem, headers, status, request)
    }

    override fun handleExceptionInternal(
        ex: Exception,
        body: Any?,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any>? {
        var finalBody = body

        if (finalBody == null) {
            finalBody = Problem.builder()
                .timestamp(OffsetDateTime.now())
                .title(HttpStatus.valueOf(status.value()).reasonPhrase)
                .status(status.value())
                .userMessage(MSG_ERRO_GENERICA_USUARIO_FINAL)
                .build()
        } else if (finalBody is String) {
            finalBody = Problem.builder()
                .timestamp(OffsetDateTime.now())
                .title(finalBody)
                .status(status.value())
                .userMessage(MSG_ERRO_GENERICA_USUARIO_FINAL)
                .build()
        }

        return super.handleExceptionInternal(ex, finalBody, headers, status, request)
    }

    private fun createProblemBuilder(
        status: HttpStatusCode,
        problemType: ProblemType,
        detail: String?
    ): Problem.Builder {
        return Problem.builder()
            .timestamp(OffsetDateTime.now())
            .status(status.value())
            .type(problemType.uri)
            .title(problemType.title)
            .detail(detail)
    }

    private fun joinPath(references: List<JsonMappingException.Reference>): String {
        return references.stream()
            .map { it.fieldName }
            .collect(Collectors.joining("."))
    }
}
