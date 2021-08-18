package br.com.zup.felipe.gadelha.infra.client

import br.com.zup.felipe.gadelha.infra.dto.request.BCBCreatePixKeyRq
import br.com.zup.felipe.gadelha.infra.dto.response.BCBCreatePixKeyRs
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Produces
import io.micronaut.http.client.annotation.Client


@Client(value = "\${application.client.bcb}")
interface BCBClient {

    @Post(value = "/api/v1/pix/keys")
    @Produces(MediaType.APPLICATION_XML)
    fun register(@Body request: BCBCreatePixKeyRq): HttpResponse<BCBCreatePixKeyRs>
}
