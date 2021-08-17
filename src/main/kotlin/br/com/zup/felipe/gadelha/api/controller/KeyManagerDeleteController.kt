package br.com.zup.felipe.gadelha.api.controller

import br.com.zup.felipe.gadelha.DeletePixRq
import br.com.zup.felipe.gadelha.DeletePixRs
import br.com.zup.felipe.gadelha.KeyManagerDeleteServiceGrpc
import br.com.zup.felipe.gadelha.domain.extensions.validate
import br.com.zup.felipe.gadelha.domain.repository.PixRepository
import io.grpc.protobuf.StatusProto
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import java.util.*
import javax.inject.Singleton

@Singleton
class KeyManagerDeleteController(
    private val repository: PixRepository,
//    private val itauClient: ItauClient
    ): KeyManagerDeleteServiceGrpc.KeyManagerDeleteServiceImplBase() {

    private val log = LoggerFactory.getLogger(KeyManagerDeleteController::class.java)

    override fun delete(request: DeletePixRq, responseObserver: StreamObserver<DeletePixRs>) {
        request.validate(repository).fold(
            ifLeft = { statusError ->
                log.error("${statusError.message}")
                responseObserver.onError(StatusProto.toStatusRuntimeException(statusError))
            },
            ifRight = { request ->
                log.info("deletando chave pix: ${request.pixId}")
                repository.deleteById(UUID.fromString(request.pixId))
                responseObserver.onNext(DeletePixRs.newBuilder().build())
            }
        )
        responseObserver.onCompleted()
    }
}