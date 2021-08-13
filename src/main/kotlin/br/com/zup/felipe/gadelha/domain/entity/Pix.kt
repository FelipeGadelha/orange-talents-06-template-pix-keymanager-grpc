package br.com.zup.felipe.gadelha.domain.entity

import br.com.zup.felipe.gadelha.AccountType
import br.com.zup.felipe.gadelha.PixKeyType
import org.hibernate.annotations.GenericGenerator
import java.util.*
import javax.persistence.*

@Entity
data class Pix(
    @Column(nullable = false, length = 36)
    val clientId: UUID,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val keyType: PixKeyType,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val accountType: AccountType
) {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    var id: UUID? = null

    @Column(nullable = false, length = 77, unique = true)
    var value: String = UUID.randomUUID().toString()

    constructor(clientId: UUID, value: String, keyType: PixKeyType, accountType: AccountType)
            : this(clientId, keyType, accountType) {
                if (value.isNotBlank()) this.value = value
            }
}
