package br.com.zup.felipe.gadelha.repository

import br.com.zup.felipe.gadelha.entity.Pix
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface PixRepository: JpaRepository<Pix, UUID> {

}
