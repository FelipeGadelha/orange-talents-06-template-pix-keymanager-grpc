package br.com.zup.felipe.gadelha.domain.extensions

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import br.com.zup.felipe.gadelha.AccountType
import br.com.zup.felipe.gadelha.PixKeyType
import br.com.zup.felipe.gadelha.PixRq
import br.com.zup.felipe.gadelha.api.handler.alreadyExistsHandler
import br.com.zup.felipe.gadelha.api.handler.invalidArgumentHandler
import br.com.zup.felipe.gadelha.api.validation.IsUUID
import br.com.zup.felipe.gadelha.api.validation.ValidAccount
import br.com.zup.felipe.gadelha.api.validation.ValidKeyType
import br.com.zup.felipe.gadelha.domain.entity.Pix
import br.com.zup.felipe.gadelha.domain.entity.TypeKey
import br.com.zup.felipe.gadelha.domain.repository.PixRepository
import com.google.rpc.Status
import java.util.*
import javax.validation.ConstraintViolationException
import javax.validation.Validator
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

fun PixRq.convertPix(validator: Validator, repository: PixRepository): Pix {
    val valid = object {
        @NotNull @IsUUID @Size(max = 36)
        val uuid: String = clientId
        @Size(max = 77)
        val key: String = value
        @NotNull @ValidKeyType
        val typeKey: PixKeyType = keyType
        @NotNull @ValidAccount
        val account: AccountType = accountType
    }
    val objectErrors = validator.validate(valid)
    if (objectErrors.isNotEmpty()) return throw ConstraintViolationException(objectErrors)

    val pix = Pix(
            clientId = UUID.fromString(valid.uuid),
            value = valid.key,
            typeKey = valid.typeKey.toString(),
            typeAccount = valid.account.toString()
        )
    val errors = validator.validate(pix)
    if (errors.isNotEmpty()) return throw ConstraintViolationException(errors)
    if(repository.existsByValueAndTypeKey(value, TypeKey.valueOf(keyType.toString())))
        return throw IllegalStateException("Chave já cadastrada: $value")
    return pix
}