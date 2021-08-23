package br.com.zup.felipe.gadelha.api.handler

import br.com.zup.felipe.gadelha.api.handler.ExceptionHandler.StatusWithDetails
import br.com.zup.felipe.gadelha.domain.exception.AlreadyExistsException
import br.com.zup.felipe.gadelha.domain.exception.OperationNotAllowedException
import javax.validation.ConstraintViolationException

class DefaultExceptionHandler: ExceptionHandler<Exception> {
    override fun handle(e: Exception): StatusWithDetails {
        val status = when (e) {
            is ConstraintViolationException -> invalidArgumentHandler(e)
            is IllegalStateException -> failedPreconditionHandler(e.message)
            is IllegalArgumentException -> failedPreconditionHandler(e.message)
            is AlreadyExistsException -> alreadyExistsHandler(e.message)
            is OperationNotAllowedException -> permissionDeniedHandler(e.message)
            else -> defaultHandler(e.message)
        }
        return StatusWithDetails(status)
    }

    override fun supports(e: Exception): Boolean {
        return true
    }
}