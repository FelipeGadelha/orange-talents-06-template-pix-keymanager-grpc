package br.com.zup.felipe.gadelha.infra.client

import br.com.zup.felipe.gadelha.infra.dto.request.BCBCreatePixKeyRq
import br.com.zup.felipe.gadelha.infra.dto.request.BCBDeletePixKeyRq
import br.com.zup.felipe.gadelha.infra.dto.response.BCBCreatePixKeyRs
import br.com.zup.felipe.gadelha.infra.dto.response.BCBDeletePixKeyRs
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import io.micronaut.http.client.annotation.Client


@Client(value = "\${application.client.bcb}")
interface BCBClient {

    @Post(value = "/api/v1/pix/keys",
        produces = [MediaType.APPLICATION_XML],
        consumes = [MediaType.APPLICATION_XML]
        )
    fun registerPix(@Body request: BCBCreatePixKeyRq): HttpResponse<BCBCreatePixKeyRs>

    @Delete(value = "/api/v1/pix/keys/{key}",
        produces = [MediaType.APPLICATION_XML],
        consumes = [MediaType.APPLICATION_XML])
    fun deletePix(@PathVariable key: String, @Body request: BCBDeletePixKeyRq): HttpResponse<BCBDeletePixKeyRs>
}
