package br.com.zup.felipe.gadelha.api.handler

import br.com.zup.felipe.gadelha.api.handler.ExceptionHandler.StatusWithDetails

class DefaultExceptionHandler: ExceptionHandler<Exception> {
    override fun handle(e: Exception): StatusWithDetails {
        val status = when (e) {
            is IllegalArgumentException -> invalidArgumentHandler(e.message)
            is IllegalStateException -> failedPreconditionHandler(e.message)
            else -> defaultHandler(e.message)
        }
        return StatusWithDetails(status!!)
    }

    override fun supports(e: Exception): Boolean {
        return true
    }
}