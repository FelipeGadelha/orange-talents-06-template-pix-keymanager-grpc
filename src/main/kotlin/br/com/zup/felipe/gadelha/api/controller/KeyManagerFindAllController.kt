package br.com.zup.felipe.gadelha.api.controller

import br.com.zup.felipe.gadelha.FindAllPixRq
import br.com.zup.felipe.gadelha.FindAllPixRs
import br.com.zup.felipe.gadelha.KeyManagerFindAllServiceGrpc
import br.com.zup.felipe.gadelha.domain.extension.validate
import br.com.zup.felipe.gadelha.domain.repository.PixRepository
import com.google.protobuf.Timestamp
import io.grpc.stub.StreamObserver
import java.time.ZoneId
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Validator

@Singleton
class KeyManagerFindAllController(
    private val repository: PixRepository,
    private val validator: Validator,
): KeyManagerFindAllServiceGrpc.KeyManagerFindAllServiceImplBase() {

    @Transactional
    override fun findAll(request: FindAllPixRq, responseObserver: StreamObserver<FindAllPixRs>) {
        val clientId = request.validate(validator)
        val result = repository.findAllByClientId(clientId).map { pix ->
            FindAllPixRs.KeyRs.newBuilder()
                .setClientId(pix.clientId.toString())
                .setPixId(pix.id.toString())
                .setValue(pix.value)
                .setKeyType(pix.typeKey.requestType)
                .setAccountType(pix.typeAccount.requestType)
                .setCreatedAt(
                    Timestamp.newBuilder()
                        .setSeconds(pix.createdAt!!.atZone(ZoneId.of("UTC")).toInstant().epochSecond)
                        .setNanos(pix.createdAt!!.atZone(ZoneId.of("UTC")).toInstant().nano)
                        .build()
                )
                .build()
        }
        responseObserver.onNext(FindAllPixRs.newBuilder()
                .addAllPixKeys(result)
            .build()
        )
        responseObserver.onCompleted()
    }
}