package br.com.zup.felipe.gadelha.api.controller

import br.com.zup.felipe.gadelha.KeyManagerServiceGrpc
import br.com.zup.felipe.gadelha.PixRq
import br.com.zup.felipe.gadelha.PixRs
import br.com.zup.felipe.gadelha.domain.extensions.convertPix
import br.com.zup.felipe.gadelha.domain.extensions.validate
import br.com.zup.felipe.gadelha.domain.repository.PixRepository
import io.grpc.protobuf.StatusProto
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import javax.inject.Singleton

@Singleton
class KeyManagerController(private val repository: PixRepository): KeyManagerServiceGrpc.KeyManagerServiceImplBase() {

    private val log = LoggerFactory.getLogger(KeyManagerController::class.java)

    override fun register(request: PixRq, responseObserver: StreamObserver<PixRs>) {
        request.validate().fold(
            { statusError ->
                log.error("${statusError.message}")
                responseObserver.onError(StatusProto.toStatusRuntimeException(statusError))
            },
            { pixRq ->
                log.info("registrando chave pix: ${pixRq.clientId}, ${pixRq.value}, ${pixRq.accountType}, ${pixRq.keyType}")
                val saved = repository.save(pixRq.convertPix())
                responseObserver.onNext(PixRs.newBuilder()
                    .setPixId(saved.id.toString())
                    .build()
                )
            }
        )
    }
}