package br.com.zup.felipe.gadelha.domain.extensions

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import br.com.zup.felipe.gadelha.*
import br.com.zup.felipe.gadelha.api.handler.alreadyExistsHandler
import br.com.zup.felipe.gadelha.domain.entity.Pix
import br.com.zup.felipe.gadelha.api.handler.invalidArgumentHandler
import br.com.zup.felipe.gadelha.api.validation.isUnknowableAccount
import br.com.zup.felipe.gadelha.api.validation.isUnrecognizableKeyType
import br.com.zup.felipe.gadelha.api.validation.keyValidator
import br.com.zup.felipe.gadelha.api.validation.validateUUID
import br.com.zup.felipe.gadelha.domain.repository.PixRepository
import com.google.rpc.Status
import java.util.*

fun PixRq.validate(repository: PixRepository): Either<Status, PixRq> {
    return when {
        clientId.isNullOrBlank() -> invalidArgumentHandler("O clientId não pode ser nulo").left()
        validateUUID(clientId) -> invalidArgumentHandler("O clientId inválido").left()
        value.length > 77 -> invalidArgumentHandler("O valor da chave não pode passar de 77 caracteres").left()
        isUnknowableAccount(accountType) -> invalidArgumentHandler("O tipo de conta é irreconhecivel").left()
        isUnrecognizableKeyType(keyType) -> invalidArgumentHandler("O tipo de chave é irreconhecivel").left()
        keyValidator(keyType, value) -> invalidArgumentHandler("O valor de chave '$value' é invalido para o tipo de chave '$keyType'").left()
        repository.existsByValueAndKeyType(value, keyType) -> alreadyExistsHandler("Chave já cadastrada: $value").left()
        else -> this.right();
    }
}

fun PixRq.convertPix(): Pix = Pix(
        clientId = UUID.fromString(clientId),
        value = value,
        keyType = keyType,
        accountType = accountType)
