package br.com.zup.felipe.gadelha.extensions

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import br.com.zup.felipe.gadelha.AccountType
import br.com.zup.felipe.gadelha.PixKeyType
import br.com.zup.felipe.gadelha.PixRq
import br.com.zup.felipe.gadelha.entity.Pix
import br.com.zup.felipe.gadelha.invalidArgumentErrorHandler
import io.grpc.StatusRuntimeException
import java.util.*
import kotlin.text.Regex as Regex

fun PixRq.validate(): Either<StatusRuntimeException, PixRq> {
    println(keyType)
    if (clientId.isNullOrBlank())
        return invalidArgumentErrorHandler("O clientId não pode ser nulo").left()
    if (!clientId.matches(RegexValidation.UUID.regex))
        return invalidArgumentErrorHandler("O clientId inválido").left()
    if (value.length > 77)
        return invalidArgumentErrorHandler("O valor da chave não pode passar de 77 caracteres").left()
    if (accountType.equals(AccountType.UNKNOWABLE))
        return invalidArgumentErrorHandler("O tipo de conta é irreconhecivel").left()
    if (keyType.equals(PixKeyType.UNRECOGNIZABLE))
        return invalidArgumentErrorHandler("O tipo de chave é irreconhecivel").left()
    if (keyType.equals(PixKeyType.CPF)
        && !value.matches(RegexValidation.CPF.regex))
        return invalidArgumentErrorHandler("O CPF é invalido para o tipo de chave pix").left()
    if (keyType.equals(PixKeyType.EMAIL)
        && !value.matches(RegexValidation.EMAIL.regex))
        return invalidArgumentErrorHandler("O EMAIL é invalido para o tipo de chave pix").left()
    if (keyType.equals(PixKeyType.CEL_PHONE)
        && !value.matches(RegexValidation.CELL_PHONE.regex))
        return invalidArgumentErrorHandler("O número de telefone é invalido para o tipo de chave pix").left()
    return this.right();
}

fun PixRq.convertPix(): Pix = Pix(
        clientId = UUID.fromString(clientId),
        value = value,
        keyType = keyType,
        accountType = accountType)

enum class RegexValidation(val regex: Regex) {
    CPF("^[0-9]{11}$".toRegex()),
    EMAIL("[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}\\@[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}(\\.[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25})+".toRegex()),
    CELL_PHONE("^+[1-9][0-9]\\d{1,14}\$".toRegex()),
    UUID("[a-f0-9]{8}(-[a-f0-9]{4}){4}[a-f0-9]{8}".toRegex());
}