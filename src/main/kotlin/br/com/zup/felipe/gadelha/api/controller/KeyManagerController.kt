package br.com.zup.felipe.gadelha.api.controller

import arrow.core.left
import arrow.core.right
import br.com.zup.felipe.gadelha.KeyManagerServiceGrpc
import br.com.zup.felipe.gadelha.PixRq
import br.com.zup.felipe.gadelha.PixRs
import br.com.zup.felipe.gadelha.api.handler.alreadyExistsHandler
import br.com.zup.felipe.gadelha.api.handler.notFoundHandler
import br.com.zup.felipe.gadelha.domain.extensions.convertPix
import br.com.zup.felipe.gadelha.domain.extensions.validate
import br.com.zup.felipe.gadelha.domain.repository.PixRepository
import br.com.zup.felipe.gadelha.infra.client.ItauClient
import io.grpc.protobuf.StatusProto
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import java.util.function.Consumer
import javax.inject.Singleton

@Singleton
class KeyManagerController(
    private val repository: PixRepository,
    private val itauClient: ItauClient
    ): KeyManagerServiceGrpc.KeyManagerServiceImplBase() {

    private val log = LoggerFactory.getLogger(KeyManagerController::class.java)

    override fun register(request: PixRq, responseObserver: StreamObserver<PixRs>) {
        log.info("registrando chave pix: ${request.clientId}, ${request.value} ${request.accountType}, ${request.keyType}")
        request.validate(repository).fold(
            ifLeft = { statusError ->
                log.error("${statusError.message}")
                responseObserver.onError(StatusProto.toStatusRuntimeException(statusError))
            },
            ifRight = { pixRq ->
                itauClient.findClient(pixRq.clientId).body.ifPresentOrElse({
                    log.info("registrando chave pix: ${pixRq.clientId}, ${pixRq.value}, ${pixRq.accountType}, ${pixRq.keyType}")
                    val saved = repository.save(pixRq.convertPix())
                    responseObserver.onNext(PixRs.newBuilder()
                        .setPixId(saved.id.toString())
                        .build()
                    )},
                    { responseObserver.onError(StatusProto.toStatusRuntimeException(notFoundHandler("Cliente não cadastrado"))) })
            }
        )
        responseObserver.onCompleted()
    }
}