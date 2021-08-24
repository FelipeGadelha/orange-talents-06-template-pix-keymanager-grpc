package br.com.zup.felipe.gadelha.api.controller

import br.com.zup.felipe.gadelha.AccountType
import br.com.zup.felipe.gadelha.FindPixRq
import br.com.zup.felipe.gadelha.FindPixRs
import br.com.zup.felipe.gadelha.KeyManagerFindServiceGrpc
import br.com.zup.felipe.gadelha.domain.extension.convertPix
import br.com.zup.felipe.gadelha.domain.repository.PixRepository
import br.com.zup.felipe.gadelha.domain.util.Institution
import br.com.zup.felipe.gadelha.infra.client.BCBClient
import com.google.protobuf.Timestamp
import io.grpc.stub.StreamObserver
import io.micronaut.http.client.exceptions.HttpClientResponseException
import java.time.ZoneId
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Validator

@Singleton
class KeyManagerFindController(
    private val repository: PixRepository,
    private val validator: Validator,
    private val bcbClient: BCBClient,
): KeyManagerFindServiceGrpc.KeyManagerFindServiceImplBase() {

    @Transactional
    override fun find(request: FindPixRq, responseObserver: StreamObserver<FindPixRs>) {
        val pix = request.convertPix(validator, repository)
        val httpResponse = bcbClient.findPix(pix.value)
        if (httpResponse.status.code != 200)
            throw HttpClientResponseException("teste", httpResponse)
        val response = httpResponse.body()
        val result = if (request.hasPixKey()) FindPixRs.newBuilder()
            else FindPixRs.newBuilder()
                .setClientId(pix.clientId.toString())
                .setPixId(pix.id.toString())
        responseObserver.onNext(
            result
                .setName(response.owner.name)
                .setCpf(response.owner.taxIdNumber)
                .setValue(pix.value)
                .setKetType(pix.typeKey.requestType)
                .setAccount(FindPixRs.AccountRs.newBuilder()
                    .setName(Institution.name(response.bankAccount.participant))
                    .setType(AccountType.valueOf(response.bankAccount.accountType.type))
                    .setNumber(response.bankAccount.accountNumber)
                    .setAgency(response.bankAccount.branch)
                    .build()
                )
                .setCreatedAt(Timestamp.newBuilder()
                    .setSeconds(response.createdAt.atZone(ZoneId.of("UTC")).toInstant().epochSecond)
                    .setNanos(response.createdAt.atZone(ZoneId.of("UTC")).toInstant().nano)
                    .build()
                )
            .build()
        )
        responseObserver.onCompleted()
    }
}