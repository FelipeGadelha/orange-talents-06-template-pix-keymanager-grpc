package br.com.zup.felipe.gadelha.domain.extensions

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import br.com.zup.felipe.gadelha.*
import br.com.zup.felipe.gadelha.api.handler.alreadyExistsHandler
import br.com.zup.felipe.gadelha.api.handler.failedPreconditionHandler
import br.com.zup.felipe.gadelha.api.handler.invalidArgumentHandler
import br.com.zup.felipe.gadelha.api.validation.isUnknowableAccount
import br.com.zup.felipe.gadelha.api.validation.isUnrecognizableKeyType
import br.com.zup.felipe.gadelha.api.validation.keyValidator
import br.com.zup.felipe.gadelha.api.validation.validateUUID
import br.com.zup.felipe.gadelha.domain.entity.Pix
import br.com.zup.felipe.gadelha.domain.entity.TypeKey
import br.com.zup.felipe.gadelha.domain.repository.PixRepository
import com.google.rpc.Status
import java.util.*
import javax.validation.ConstraintViolationException
import javax.validation.Validator

fun PixRq.validate(repository: PixRepository): Either<Status, Pix> {
    return when {
        clientId.isNullOrBlank() -> failedPreconditionHandler("O clientId não pode ser nulo").left()
        validateUUID(clientId) -> failedPreconditionHandler("O clientId inválido").left()
        value.length > 77 -> failedPreconditionHandler("O valor da chave não pode passar de 77 caracteres").left()
        isUnknowableAccount(accountType) -> failedPreconditionHandler("O tipo de conta é irreconhecivel").left()
        isUnrecognizableKeyType(keyType) -> failedPreconditionHandler("O tipo de chave é irreconhecivel").left()
        keyValidator(keyType, value) -> failedPreconditionHandler("O valor de chave '$value' é invalido para o tipo de chave '$keyType'").left()
        repository.existsByValueAndTypeKey(value, TypeKey.valueOf(keyType.toString())) -> alreadyExistsHandler("Chave já cadastrada: $value").left()
        else -> this.convertPix().right();
    }
}

fun PixRq.convertPix(): Pix = Pix(
        clientId = UUID.fromString(clientId),
        value = value,
        typeKey = keyType.toString(),
        accountType = accountType.toString()
    )


//fun PixRq.convertPix(validator: Validator): Either<Status, Pix> {
//    val pix = Pix(
//            clientId = UUID.fromString(clientId),
//            value = value,
//            typeKey = keyType.toString(),
//            accountType = accountType.toString()
//        )
//    val errors = validator.validate(pix)
//    if (errors.isNotEmpty()) throw ConstraintViolationException(errors)
//    return pix.right()
//}