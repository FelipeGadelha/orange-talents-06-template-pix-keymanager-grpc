package br.com.zup.felipe.gadelha.domain.service

import br.com.zup.felipe.gadelha.api.handler.notFoundHandler
import br.com.zup.felipe.gadelha.domain.entity.Pix
import br.com.zup.felipe.gadelha.infra.client.ItauClient
import io.grpc.protobuf.StatusProto
import io.netty.handler.codec.http.HttpResponseStatus
import javax.inject.Singleton

@Singleton
class ItauProcess(
    private val itauClient: ItauClient,
) {

    fun register(pix: Pix) {
        val itauResponse = itauClient.findAccountClient(pix.clientId.toString(), pix.typeAccount.itau)
        if (itauResponse.status.code != HttpResponseStatus.OK.code()) {
            StatusProto.toStatusRuntimeException(notFoundHandler("Cliente não cadastrado no Itaú"))
        }
    }
}