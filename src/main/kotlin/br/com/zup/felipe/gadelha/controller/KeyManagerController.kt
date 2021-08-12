package br.com.zup.felipe.gadelha.controller

import br.com.zup.felipe.gadelha.*
import br.com.zup.felipe.gadelha.extensions.convertPix
import br.com.zup.felipe.gadelha.extensions.validate
import br.com.zup.felipe.gadelha.repository.PixRepository
import io.grpc.stub.StreamObserver
import io.micronaut.http.annotation.Controller
import org.slf4j.LoggerFactory
import java.util.UUID.randomUUID
import javax.inject.Singleton

@Singleton
class KeyManagerController(private val repository: PixRepository): KeyManagerServiceGrpc.KeyManagerServiceImplBase() {

    private val log = LoggerFactory.getLogger(KeyManagerController::class.java)

    override fun register(request: PixRq, responseObserver: StreamObserver<PixRs>) {
        request.validate().fold(
            { statusError ->
                log.error("${statusError.message}")
                responseObserver.onError(statusError)
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
        responseObserver.onCompleted()
    }
}