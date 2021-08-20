package br.com.zup.felipe.gadelha.api.handler

import br.com.zup.felipe.gadelha.api.handler.ExceptionHandler.StatusWithDetails
import javax.validation.ConstraintViolationException

class DefaultExceptionHandler: ExceptionHandler<Exception> {
    override fun handle(e: Exception): StatusWithDetails {
        val status = when (e) {
            is ConstraintViolationException -> invalidArgumentHandler(e)
            is IllegalStateException -> alreadyExistsHandler(e.message)
            is IllegalArgumentException -> failedPreconditionHandler(e.message)
            else -> defaultHandler(e.message)
        }
        return StatusWithDetails(status)
    }

    override fun supports(e: Exception): Boolean {
        return true
    }
}