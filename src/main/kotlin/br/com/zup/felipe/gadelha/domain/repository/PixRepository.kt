package br.com.zup.felipe.gadelha.domain.repository

import br.com.zup.felipe.gadelha.domain.entity.Pix
import br.com.zup.felipe.gadelha.domain.entity.TypeKey
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface PixRepository: JpaRepository<Pix, UUID> {
    fun existsByValueAndTypeKey(value: String, typeKey: TypeKey): Boolean
    fun existsByIdAndClientId(id: UUID, clientId: UUID): Boolean
    fun findByIdAndClientId(id: UUID, clientId: UUID): Optional<Pix>
    fun findByValue(key: String): Optional<Pix>
}
