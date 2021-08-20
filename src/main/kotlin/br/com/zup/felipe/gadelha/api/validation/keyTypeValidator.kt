package br.com.zup.felipe.gadelha.api.validation

import br.com.zup.felipe.gadelha.AccountType
import br.com.zup.felipe.gadelha.PixKeyType
import br.com.zup.felipe.gadelha.domain.entity.TypeKey
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
@Constraint(validatedBy = [KeyTypeValidator::class])
annotation class ValidKeyType(
    val message: String = "O tipo de chave é irreconhecivel",
    val groups: Array<KClass<Any>> = [],
    val payload: Array<KClass<Payload>> = []
)

@Singleton
open class KeyTypeValidator: ConstraintValidator<ValidKeyType, PixKeyType> {
    override fun isValid(
        value: PixKeyType?,
        annotationMetadata: AnnotationValue<ValidKeyType>,
        context: ConstraintValidatorContext
    ): Boolean = value != PixKeyType.UNRECOGNIZABLE
}
