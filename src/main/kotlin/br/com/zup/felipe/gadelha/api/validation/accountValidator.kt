package br.com.zup.felipe.gadelha.api.validation

import br.com.zup.felipe.gadelha.AccountType
import io.micronaut.core.annotation.AnnotationValue
import io.micronaut.validation.validator.constraints.ConstraintValidator
import io.micronaut.validation.validator.constraints.ConstraintValidatorContext
import javax.inject.Singleton
import javax.validation.Constraint
import javax.validation.Payload
import kotlin.reflect.KClass

@MustBeDocumented
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [AccountValidator::class])
annotation class ValidAccount(
    val message: String = "O tipo de conta é irreconhecivel",
    val groups: Array<KClass<Any>> = [],
    val payload: Array<KClass<Payload>> = []
)

@Singleton
open class AccountValidator: ConstraintValidator<ValidAccount, AccountType> {

    override fun isValid(
        value: AccountType?,
        annotationMetadata: AnnotationValue<ValidAccount>,
        context: ConstraintValidatorContext
    ): Boolean = value != AccountType.UNKNOWABLE
}