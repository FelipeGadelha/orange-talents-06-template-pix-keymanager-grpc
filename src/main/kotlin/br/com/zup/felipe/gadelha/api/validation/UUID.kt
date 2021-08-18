package br.com.zup.felipe.gadelha.api.validation

import io.micronaut.core.annotation.AnnotationValue
import io.micronaut.validation.validator.constraints.ConstraintValidator
import io.micronaut.validation.validator.constraints.ConstraintValidatorContext
import javax.inject.Singleton
import javax.validation.Constraint
import javax.validation.Payload
import kotlin.reflect.KClass

@MustBeDocumented
@Target(AnnotationTarget.FIELD, AnnotationTarget.CONSTRUCTOR)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [IsUUIDValidator::class])
annotation class IsUUID(
    val message: String = "Esta Entidade já existe no banco de dados",
    val fieldName: String,
    val domainClass: KClass<*>,
    val groups: Array<KClass<Any>> = [],
    val payload: Array<KClass<Payload>> = []
)

@Singleton
open class IsUUIDValidator: ConstraintValidator<IsUUID, Any> {

    override fun isValid(
        value: Any?,
        annotationMetadata: AnnotationValue<IsUUID>,
        context: ConstraintValidatorContext
    ): Boolean {
        TODO("Not yet implemented")
    }


}