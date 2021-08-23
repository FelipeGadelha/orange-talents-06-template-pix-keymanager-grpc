package br.com.zup.felipe.gadelha.infra.client

import br.com.zup.felipe.gadelha.infra.dto.response.AccountItauRs
import br.com.zup.felipe.gadelha.infra.dto.response.ClientItauRs
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client

@Client(value = "\${application.client.itau}")
interface ItauClient {

    @Get(value = "/api/v1/clientes/{clientId}/contas")
    fun findAccountClient(@PathVariable clientId: String, @QueryValue("tipo") accountType: String): HttpResponse<AccountItauRs>

    @Get(value = "/api/v1/clientes/{clientId}")
    fun findClientItau(@PathVariable clientId: String): HttpResponse<ClientItauRs>

}
