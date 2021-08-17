package br.com.zup.felipe.gadelha.domain.repository

import br.com.zup.felipe.gadelha.PixKeyType
import br.com.zup.felipe.gadelha.domain.entity.Pix
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface PixRepository: JpaRepository<Pix, UUID> {
    fun existsByValueAndKeyType(value: String, keyType: PixKeyType): Boolean
    fun existsByIdAndClientId(id: UUID, clientId: UUID): Boolean
}
