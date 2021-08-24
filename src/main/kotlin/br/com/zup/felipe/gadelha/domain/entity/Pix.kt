package br.com.zup.felipe.gadelha.domain.entity

import br.com.zup.felipe.gadelha.PixKeyType
import br.com.zup.felipe.gadelha.api.validation.PixValue
import org.hibernate.annotations.GenericGenerator
import java.util.*
import javax.persistence.*

@Entity
@PixValue
data class Pix(
    @Column(nullable = false, length = 36)
    val clientId: UUID,

    @Column(nullable = false, length = 77, unique = true)
    val value: String,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val typeKey: TypeKey,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val typeAccount: TypeAccount,

    @Column(nullable = false)
    val participant: String?
) {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    var id: UUID? = null


    constructor(clientId: UUID, value: String, typeKey: String, typeAccount: String)
            : this(clientId, value, TypeKey.valueOf(typeKey), TypeAccount.valueOf(typeAccount), null)
}

enum class TypeAccount(val itau: String) {
    CURRENT("CONTA_CORRENTE"),
    SAVING("CONTA_POUPANCA")
}
enum class TypeKey(val requestType: PixKeyType){
    CPF(PixKeyType.CPF),
    PHONE(PixKeyType.PHONE),
    EMAIL(PixKeyType.EMAIL),
    RANDOM(PixKeyType.RANDOM);
}