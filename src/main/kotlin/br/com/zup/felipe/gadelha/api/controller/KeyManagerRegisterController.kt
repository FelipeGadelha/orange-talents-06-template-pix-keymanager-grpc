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
import br.com.zup.felipe.gadelha.infra.dto.request.BCBCreatePixKeyRq
import io.grpc.protobuf.StatusProto
import io.grpc.stub.StreamObserver
import io.netty.handler.codec.http.HttpResponseStatus.CREATED
import io.netty.handler.codec.http.HttpResponseStatus.OK
import org.slf4j.LoggerFactory
import javax.inject.Singleton
import javax.validation.Validator

@Singleton
class KeyManagerRegisterController(
    private val repository: PixRepository,
    private val itauClient: ItauClient,
    private val bcbClient: BCBClient,
    private val validator: Validator
    ): KeyManagerRegisterServiceGrpc.KeyManagerRegisterServiceImplBase() {

    private val log = LoggerFactory.getLogger(KeyManagerRegisterController::class.java)

    override fun register(request: PixRq, responseObserver: StreamObserver<PixRs>) {
        log.info("registrando chave pix: ${request.clientId}, ${request.value}, ${request.accountType}, ${request.keyType}")
//        request.convertPix(validator)
//        responseObserver.onNext(PixRs.newBuilder().build())

        request.validate(repository).fold(
            ifLeft = { statusError ->
                log.error("${statusError.message}")
                responseObserver.onError(StatusProto.toStatusRuntimeException(statusError))
            },
            ifRight = { pix ->
                val itauResponse = itauClient.findAccountClient(pix.clientId.toString(), pix.typeAccount.itau)
                if(itauResponse.status.code != OK.code()){
                    responseObserver.onError(
                        StatusProto.toStatusRuntimeException(notFoundHandler("Cliente não cadastrado no Itaú")))
                    return
                }
                val bcbResponse = bcbClient.register(BCBCreatePixKeyRq(itauResponse.body(), pix))
                println(bcbResponse.status)
                println(bcbResponse.body())
                if (bcbResponse.status.code != CREATED.code()) {
                    responseObserver.onError(
                        StatusProto.toStatusRuntimeException(notFoundHandler("Não foi possível registrar chave pix")))
                    return
                }
                println(bcbResponse)
                log.info("registrando chave pix: ${pix.clientId}, ${pix.value}, ${pix.typeKey}, ${pix.typeAccount}")
                val saved = repository.save(pix)
                responseObserver.onNext(PixRs.newBuilder()
                    .setPixId(saved.id.toString())
                    .build())
            }
        )
        responseObserver.onCompleted()
    }
}