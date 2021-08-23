package br.com.zup.felipe.gadelha.domain.extension

import br.com.zup.felipe.gadelha.DeletePixRq
import br.com.zup.felipe.gadelha.api.validation.IsUUID
import br.com.zup.felipe.gadelha.domain.entity.Pix
import br.com.zup.felipe.gadelha.domain.exception.OperationNotAllowedException
import br.com.zup.felipe.gadelha.domain.repository.PixRepository
import java.util.*
import javax.validation.ConstraintViolationException
import javax.validation.Validator
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

fun DeletePixRq.validate(validator: Validator, repository: PixRepository): Pix {

    val valid = object {
        @NotNull
        @IsUUID
        @Size(max = 36)
        val client: String = clientId

        @NotNull
        @IsUUID
        @Size(max = 36)
        val key: String = pixId
    }
    val objectErrors = validator.validate(valid)
    if (objectErrors.isNotEmpty()) return throw ConstraintViolationException(objectErrors)
    return repository
        .findByIdAndClientId(UUID.fromString(valid.key), UUID.fromString(valid.client))
        .orElseThrow{ OperationNotAllowedException("Chave Pix não encontrado ou não pertence ao cliente") }

//     when {
//         clientId.isNullOrBlank() -> alreadyExistsHandler("O clientId não pode ser nulo").left()
////         isUUID(clientId) -> alreadyExistsHandler("ClientId inválido").left()
//         pixId.isNullOrBlank() -> alreadyExistsHandler("O pixId não pode ser nulo").left()
//         !repository.existsByIdAndClientId(UUID.fromString(pixId), UUID.fromString(clientId))
//            -> permissionDeniedHandler("Chave Pix não encontrado ou não pertence ao cliente").left()
//         else -> this.right();
//    }
}

