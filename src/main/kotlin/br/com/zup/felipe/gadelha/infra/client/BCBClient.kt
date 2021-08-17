package br.com.zup.felipe.gadelha.infra.client

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Post
import io.micronaut.http.client.annotation.Client


@Client(value = "\${application.client.bcb}")
interface BCBClient {

    @Post(value = "/api/v1/pix/keys")
    fun register(request: CreatePixKeyRequest): HttpResponse<ItauClientRs>

}

data class CreatePixKeyRequest(val keyType: String)