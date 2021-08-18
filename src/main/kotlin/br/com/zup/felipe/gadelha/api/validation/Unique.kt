package br.com.zup.felipe.gadelha.api.validation

import io.micronaut.core.annotation.AnnotationValue
import io.micronaut.validation.validator.constraints.ConstraintValidator
import io.micronaut.validation.validator.constraints.ConstraintValidatorContext
import javax.inject.Singleton
import javax.persistence.EntityManager
import javax.transaction.Transactional
import javax.validation.Constraint
import javax.validation.Payload
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.CONSTRUCTOR
import kotlin.annotation.AnnotationTarget.FIELD
import kotlin.reflect.KClass

@MustBeDocumented
@Target(FIELD, CONSTRUCTOR)
@Retention(RUNTIME)
@Constraint(validatedBy = [UniqueValidator::class])
annotation class Unique(
    val message: String = "Esta Entidade já existe no banco de dados",
    val fieldName: String,
    val domainClass: KClass<*>,
    val groups: Array<KClass<Any>> = [],
    val payload: Array<KClass<Payload>> = []
)

@Singleton
open class UniqueValidator(
    private var manager: EntityManager
): ConstraintValidator<Unique, Any> {

    private lateinit var fieldId: String
    private lateinit var klass: KClass<*>

    override fun initialize(annotation: Unique?) {
        this.klass = annotation!!.domainClass
        this.fieldId = annotation.fieldName
    }

    @Transactional
    override fun isValid(
        value: Any?,
        annotationMetadata: AnnotationValue<Unique>,
        context: ConstraintValidatorContext
    ): Boolean {
        return manager.createQuery("select 1 from ${klass.qualifiedName} where $fieldId =:pValue")
            .setParameter("pValue", value)
            .resultList
            .isEmpty();
    }
}