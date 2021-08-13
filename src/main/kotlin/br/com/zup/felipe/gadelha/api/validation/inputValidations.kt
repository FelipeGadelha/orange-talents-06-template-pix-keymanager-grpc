package br.com.zup.felipe.gadelha.api.validation

import br.com.zup.felipe.gadelha.AccountType
import br.com.zup.felipe.gadelha.PixKeyType

fun validateUUID(value: String): Boolean =  !RegexValidator.UUID.validate(value)

fun isUnknowableAccount(accountType: AccountType): Boolean = accountType == AccountType.UNKNOWABLE

fun isUnrecognizableKeyType(keyType: PixKeyType): Boolean = keyType == PixKeyType.UNRECOGNIZABLE

fun keyValidator(keyType: PixKeyType, value: String): Boolean =
    when(keyType) {
        PixKeyType.CPF -> !RegexValidator.CPF.validate(value)
        PixKeyType.EMAIL -> !RegexValidator.EMAIL.validate(value)
        PixKeyType.CEL_PHONE -> !RegexValidator.CELL_PHONE.validate(value)
        PixKeyType.RANDOM_KEY -> value.isNotBlank()
        else -> false
    }


