package br.com.zup.felipe.gadelha.api.controller

import br.com.zup.felipe.gadelha.DeletePixRq
import br.com.zup.felipe.gadelha.DeletePixRs
import br.com.zup.felipe.gadelha.KeyManagerDeleteServiceGrpc
import br.com.zup.felipe.gadelha.domain.extension.convertPix
import br.com.zup.felipe.gadelha.domain.repository.PixRepository
import br.com.zup.felipe.gadelha.infra.client.BCBClient
import br.com.zup.felipe.gadelha.infra.dto.request.BCBDeletePixKeyRq
import io.grpc.stub.StreamObserver
import io.netty.handler.codec.http.HttpResponseStatus.OK
import org.slf4j.LoggerFactory
import java.util.*
import javax.inject.Singleton
import javax.validation.Validator

@Singleton
class KeyManagerDeleteController(
    private val repository: PixRepository,
    private val bcbClient: BCBClient,
    private val validator: Validator
    ): KeyManagerDeleteServiceGrpc.KeyManagerDeleteServiceImplBase() {

    private val log = LoggerFactory.getLogger(KeyManagerDeleteController::class.java)

    override fun delete(request: DeletePixRq, responseObserver: StreamObserver<DeletePixRs>) {
        val pix = request.convertPix(validator, repository)

        val response = bcbClient.deletePix(key = pix.value, request = BCBDeletePixKeyRq(
                key = pix.value,
                participant = pix.participant!!,
            )
        )
        if (response.status.code != OK.code())
            throw IllegalStateException("Cliente não cadastrado no Itaú")
        repository.deleteById(UUID.fromString(request.pixId)).also {
            log.info("deletando chave pix: ${request.pixId}")}
        responseObserver.onNext(DeletePixRs.newBuilder().build())
        responseObserver.onCompleted()
    }
}

