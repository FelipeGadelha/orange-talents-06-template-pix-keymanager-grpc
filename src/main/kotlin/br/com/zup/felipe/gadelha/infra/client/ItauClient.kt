package br.com.zup.felipe.gadelha.infra.client

import arrow.core.Either
import br.com.zup.felipe.gadelha.infra.dto.response.ClientRs
import com.google.rpc.Status
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.client.annotation.Client

@Client(value = "http://localhost:9091")
interface ItauClient {

    @Get(value = "/api/v1/clientes/{clientId}")
    fun findClient(@PathVariable clientId: String): HttpResponse<ClientRs>
//    c56dfef4-7901-44fb-84e2-a2cefb157890
}
