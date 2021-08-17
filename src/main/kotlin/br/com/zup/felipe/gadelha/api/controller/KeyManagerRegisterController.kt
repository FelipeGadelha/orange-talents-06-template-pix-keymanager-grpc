package br.com.zup.felipe.gadelha.api.controller

import br.com.zup.felipe.gadelha.KeyManagerRegisterServiceGrpc
import br.com.zup.felipe.gadelha.PixRq
import br.com.zup.felipe.gadelha.PixRs
import br.com.zup.felipe.gadelha.api.handler.notFoundHandler
import br.com.zup.felipe.gadelha.domain.extensions.convertPix
import br.com.zup.felipe.gadelha.domain.extensions.validate
import br.com.zup.felipe.gadelha.domain.repository.PixRepository
import br.com.zup.felipe.gadelha.infra.client.BCBClient
import br.com.zup.felipe.gadelha.infra.client.ItauClient
import io.grpc.protobuf.StatusProto
import io.grpc.stub.StreamObserver
import io.netty.handler.codec.http.HttpResponseStatus.OK
import org.slf4j.LoggerFactory
import javax.inject.Singleton

@Singleton
class KeyManagerRegisterController(
    private val repository: PixRepository,
    private val itauClient: ItauClient,
    private val bcbClient: BCBClient,
    ): KeyManagerRegisterServiceGrpc.KeyManagerRegisterServiceImplBase() {

    private val log = LoggerFactory.getLogger(KeyManagerRegisterController::class.java)

    override fun register(request: PixRq, responseObserver: StreamObserver<PixRs>) {
        log.info("registrando chave pix: ${request.clientId}, ${request.value} ${request.accountType}, ${request.keyType}")
        request.validate(repository).fold(
            ifLeft = { statusError ->
                log.error("${statusError.message}")
                responseObserver.onError(StatusProto.toStatusRuntimeException(statusError))
            },
            ifRight = { pixRq ->
                when(itauClient.findClient(pixRq.clientId).status.code) {
                    OK.code() -> {
                        log.info("registrando chave pix: ${pixRq.clientId}, ${pixRq.value}, ${pixRq.accountType}, ${pixRq.keyType}")
                        val saved = repository.save(pixRq.convertPix())
                        responseObserver.onNext(PixRs.newBuilder()
                            .setPixId(saved.id.toString())
                            .build())
                    }
                    else -> responseObserver.onError(StatusProto.toStatusRuntimeException(notFoundHandler("Cliente não cadastrado no Itaú")))
                }
            }
        )
        responseObserver.onCompleted()
    }
}