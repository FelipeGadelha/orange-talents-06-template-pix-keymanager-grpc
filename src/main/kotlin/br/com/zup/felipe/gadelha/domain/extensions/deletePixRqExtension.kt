package br.com.zup.felipe.gadelha.domain.extensions

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import br.com.zup.felipe.gadelha.DeletePixRq
import br.com.zup.felipe.gadelha.api.handler.invalidArgumentHandler
import br.com.zup.felipe.gadelha.api.handler.permissionDeniedHandler
import br.com.zup.felipe.gadelha.api.validation.validateUUID
import br.com.zup.felipe.gadelha.domain.repository.PixRepository
import com.google.rpc.Status
import java.util.*

fun DeletePixRq.validate(repository: PixRepository): Either<Status, DeletePixRq> =
     when {
         clientId.isNullOrBlank() -> invalidArgumentHandler("O clientId não pode ser nulo").left()
         validateUUID(clientId) -> invalidArgumentHandler("ClientId inválido").left()
         pixId.isNullOrBlank() -> invalidArgumentHandler("O pixId não pode ser nulo").left()
         !repository.existsByIdAndClientId(UUID.fromString(pixId), UUID.fromString(clientId))
            -> permissionDeniedHandler("Chave Pix não encontrado ou não pertence ao cliente").left()
         else -> this.right();
    }
