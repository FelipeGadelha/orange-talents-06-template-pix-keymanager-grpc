package br.com.zup.felipe.gadelha.domain.extension

import br.com.zup.felipe.gadelha.*
import br.com.zup.felipe.gadelha.api.validation.IsUUID
import br.com.zup.felipe.gadelha.api.validation.ValidAccount
import br.com.zup.felipe.gadelha.api.validation.ValidKeyType
import br.com.zup.felipe.gadelha.domain.entity.Pix
import br.com.zup.felipe.gadelha.domain.entity.TypeKey
import br.com.zup.felipe.gadelha.domain.exception.AlreadyExistsException
import br.com.zup.felipe.gadelha.domain.exception.EntityNotFountException
import br.com.zup.felipe.gadelha.domain.exception.OperationNotAllowedException
import br.com.zup.felipe.gadelha.domain.repository.PixRepository
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
        return throw AlreadyExistsException("Chave já cadastrada: $value")
    return pix
}

fun DeletePixRq.convertPix(validator: Validator, repository: PixRepository): Pix {

    val valid = object {
        @NotNull @IsUUID @Size(max = 36)
        val client: String = clientId

        @NotNull @IsUUID @Size(max = 36)
        val key: String = pixId
    }
    val objectErrors = validator.validate(valid)
    if (objectErrors.isNotEmpty()) return throw ConstraintViolationException(objectErrors)
    return repository
        .findByIdAndClientId(UUID.fromString(valid.key), UUID.fromString(valid.client))
        .orElseThrow{ OperationNotAllowedException("Chave Pix não encontrado ou não pertence ao cliente") }
}

fun FindPixRq.convertPix(validator: Validator, repository: PixRepository): Pix =
    if (hasPixKey()) {
        val valid = object {
            @NotNull @NotBlank @Size(max = 77)
            val key: String = pixKey
        }
        val objectErrors = validator.validate(valid)
        if (objectErrors.isNotEmpty()) return throw ConstraintViolationException(objectErrors)
        repository
            .findByValue(valid.key)
            .orElseThrow{ EntityNotFountException("Chave Pix não encontrado") }
    }else {
        val valid = object {
            @NotNull @IsUUID @Size(max = 36)
            val client: String = pixId.clientId

            @NotNull @IsUUID @Size(max = 36)
            val id: String = pixId.pixId
        }
        val objectErrors = validator.validate(valid)
        if (objectErrors.isNotEmpty()) return throw ConstraintViolationException(objectErrors)
        repository
            .findByIdAndClientId(UUID.fromString(valid.id), UUID.fromString(valid.client))
            .orElseThrow{ EntityNotFountException("Chave Pix não encontrado") }
    }

