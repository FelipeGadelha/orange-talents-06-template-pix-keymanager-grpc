package br.com.zup.felipe.gadelha.api.controller

import br.com.zup.felipe.gadelha.KeyManagerRegisterServiceGrpc
import br.com.zup.felipe.gadelha.PixRq
import br.com.zup.felipe.gadelha.PixRs
import br.com.zup.felipe.gadelha.api.handler.notFoundHandler
import br.com.zup.felipe.gadelha.domain.entity.Pix
import br.com.zup.felipe.gadelha.domain.extension.convertPix
import br.com.zup.felipe.gadelha.domain.repository.PixRepository
import br.com.zup.felipe.gadelha.infra.client.BCBClient
import br.com.zup.felipe.gadelha.infra.client.ItauClient
import br.com.zup.felipe.gadelha.infra.dto.request.BCBCreatePixKeyRq
import io.grpc.protobuf.StatusProto
import io.grpc.stub.StreamObserver
import io.netty.handler.codec.http.HttpResponseStatus.*
import org.slf4j.LoggerFactory
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Validator

@Singleton
class KeyManagerRegisterController(
    private val repository: PixRepository,
    private val itauClient: ItauClient,
    private val bcbClient: BCBClient,
    private val validator: Validator
    ): KeyManagerRegisterServiceGrpc.KeyManagerRegisterServiceImplBase() {

    private val log = LoggerFactory.getLogger(KeyManagerRegisterController::class.java)

//"error": "2 UNKNOWN: <Problem><type>UNPROCESSABLE_ENTITY</type><status>422</status><title>Unprocessable Entity</title><detail>The informed Pix key exists already</detail></Problem>"
    @Transactional
    override fun register(request: PixRq, responseObserver: StreamObserver<PixRs>) {
        var pix = request.convertPix(validator, repository)
        val itauResponse = itauClient.findAccountClient(pix.clientId.toString(), pix.typeAccount.itau)
        if(itauResponse.status.code != OK.code()){
            responseObserver.onError(
                StatusProto.toStatusRuntimeException(notFoundHandler("Cliente não cadastrado no Itaú")))
            return
        }
    pix = pix.copy(participant = itauResponse.body().institution.ispb)
    itauResponse.body.get().institution.ispb
    val bcbResponse = bcbClient.registerPix(BCBCreatePixKeyRq(itauResponse.body(), pix))
        if (bcbResponse.status.code != CREATED.code()) {
            responseObserver.onError(
                StatusProto.toStatusRuntimeException(notFoundHandler("Não foi possível registrar chave pix")))
            return
        }
        println(bcbResponse)
        pix = bcbResponse.body.map { pix.copy(value = it.key) }.get()
        val saved = repository.save(pix).also {
            log.info("registrando chave pix: ${it.clientId}, ${it.value}, ${it.typeKey}, ${it.typeAccount}") }
        responseObserver.onNext(PixRs.newBuilder()
            .setPixId(saved.id.toString())
            .build())
        responseObserver.onCompleted()
    }
}
