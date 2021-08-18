package br.com.zup.felipe.gadelha.domain.entity

import org.hibernate.annotations.GenericGenerator
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Entity
data class Pix(
    @field:NotNull
    @Column(nullable = false, length = 36)
    val clientId: UUID,

    @field:NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val typeKey: TypeKey,

    @field:NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val typeAccount: TypeAccount
) {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    var id: UUID? = null

    @field:NotNull
    @field:Size(max = 77)
    @Column(nullable = false, length = 77, unique = true)
    var value: String = UUID.randomUUID().toString()

    constructor(clientId: UUID, value: String, typeKey: String, accountType: String)
            : this(clientId, TypeKey.valueOf(typeKey), TypeAccount.valueOf(accountType)) {
                if (value.isNotBlank()) this.value = value
            }
}

enum class TypeAccount(val itau: String) {
    CURRENT("CONTA_CORRENTE"),
    SAVING("CONTA_POUPANCA")
}
enum class TypeKey{
    CPF,
    PHONE,
    EMAIL,
    RANDOM;
}