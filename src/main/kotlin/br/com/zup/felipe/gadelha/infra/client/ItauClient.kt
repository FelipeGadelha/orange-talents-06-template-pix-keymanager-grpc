package br.com.zup.felipe.gadelha.infra.client

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.client.annotation.Client
import javax.validation.constraints.NotBlank

@Client(value = "\${application.client.itau}")
interface ItauClient {

    @Get(value = "/api/v1/clientes/{clientId}")
    fun findClient(@PathVariable clientId: String): HttpResponse<ItauClientRs>
}

data class ItauClientRs(@field:NotBlank val id: String)