package br.com.zup.felipe.gadelha.api.validation

import javax.validation.Constraint
import javax.validation.Payload
import javax.validation.constraints.Pattern
import kotlin.reflect.KClass

@MustBeDocumented
@Target(AnnotationTarget.FIELD, AnnotationTarget.CONSTRUCTOR)
@Retention(AnnotationRetention.RUNTIME)
@Pattern(regexp = "[a-f0-9]{8}(-[a-f0-9]{4}){4}[a-f0-9]{8}$",
    flags = [Pattern.Flag.CASE_INSENSITIVE])
@Constraint(validatedBy = [])
annotation class IsUUID(
    val message: String = "formato de dado inválido",
    val groups: Array<KClass<Any>> = [],
    val payload: Array<KClass<Payload>> = []
)