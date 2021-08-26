package br.com.zup.felipe.gadelha.api.controller

import br.com.zup.felipe.gadelha.FindAllPixRq
import br.com.zup.felipe.gadelha.KeyManagerFindAllServiceGrpc
import br.com.zup.felipe.gadelha.domain.entity.Pix
import br.com.zup.felipe.gadelha.domain.entity.TypeAccount
import br.com.zup.felipe.gadelha.domain.entity.TypeKey
import br.com.zup.felipe.gadelha.domain.repository.PixRepository
import com.github.javafaker.Faker
import com.google.rpc.BadRequest
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.protobuf.StatusProto
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class KeyManagerFindAllControllerTest(
    private val grpcClient: KeyManagerFindAllServiceGrpc.KeyManagerFindAllServiceBlockingStub,
    private val repository: PixRepository,
){

    private val faker: Faker = Faker(Locale("pt-BR"))

    private val clientId: String = "c56dfef4-7901-44fb-84e2-a2cefb157890"
    private lateinit var pixEmail: Pix
    private lateinit var pixRandom: Pix

    @BeforeEach
    internal fun setup() {
        repository.deleteAll()
        pixEmail = repository.save(
                Pix(
                clientId = UUID.fromString(clientId),
                value = faker.internet().emailAddress(),
                typeKey = TypeKey.EMAIL,
                typeAccount = TypeAccount.SAVING,
                participant = "60701190",
                createdAt = LocalDateTime.now()
            )
        )
        pixRandom = repository.save(
            Pix(
                clientId = UUID.fromString(clientId),
                value = UUID.randomUUID().toString(),
                typeKey = TypeKey.RANDOM,
                typeAccount = TypeAccount.SAVING,
                participant = "60701190",
                createdAt = LocalDateTime.now()
            )
        )
    }

    @Test
    internal fun `should findAll pix successfully`() {
        val response = grpcClient.findAll(
                FindAllPixRq.newBuilder()
                    .setClientId(clientId)
                    .build()
            )
        with(response){
            assertEquals(2, pixKeysCount)
            assertEquals(pixEmail.id.toString(), getPixKeys(0).pixId)
            assertEquals(pixEmail.clientId.toString(), getPixKeys(0).clientId)
            assertEquals(pixEmail.typeKey.name, getPixKeys(0).keyType.name)
            assertEquals(
                pixEmail.createdAt!!.atZone(ZoneId.of("UTC")).toInstant().epochSecond,
                getPixKeys(0).createdAt.seconds
            )
            assertEquals(pixEmail.typeAccount.name, getPixKeys(0).accountType.name)
            assertEquals(pixEmail.value, getPixKeys(0).value)
            assertEquals(pixRandom.id.toString(), getPixKeys(1).pixId)
            assertEquals(pixRandom.clientId.toString(), getPixKeys(1).clientId)
            assertEquals(pixRandom.typeKey.name, getPixKeys(1).keyType.name)
            assertEquals(
                pixRandom.createdAt!!.atZone(ZoneId.of("UTC")).toInstant().epochSecond,
                getPixKeys(1).createdAt.seconds
            )
            assertEquals(pixRandom.typeAccount.name, getPixKeys(1).accountType.name)
            assertEquals(pixRandom.value, getPixKeys(1).value)
        }
    }
    @Test
    internal fun `should return empty list`() {
        repository.deleteAll()
        val response = grpcClient.findAll(
            FindAllPixRq.newBuilder()
                .setClientId(clientId)
                .build()
        )
        with(response){ assertTrue(pixKeysList.isEmpty()) }
    }

    @Test
    internal fun `should return an exception for invalid clientId`() {
        repository.deleteAll()
        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.findAll(
                FindAllPixRq.newBuilder()
                    .setClientId("freu543y8oewqrhfds84e53")
                    .build()
            )
        }
        with(exception){
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("INVALID_ARGUMENT: Dados inválidos", this.message)
            assertEquals(
                listOf(Pair("client", "deve corresponder a \"[a-f0-9]{8}(-[a-f0-9]{4}){4}[a-f0-9]{8}\$\"")),
                StatusProto.fromThrowable(this).let { status ->
                    val badRequest = status!!.detailsList[0].unpack(BadRequest::class.java)
                    badRequest.fieldViolationsList.map { it.field to it.description }
                }
            )
        }
    }

    @Test
    internal fun `should return an exception for clientId null`() {
        repository.deleteAll()
        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.findAll(
                FindAllPixRq.newBuilder()
                    .setClientId("freu543y8oewqrhfds84e53")
                    .build()
            )
        }
    }

    @Factory
    class FindAllClients {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeyManagerFindAllServiceGrpc.KeyManagerFindAllServiceBlockingStub {
            return KeyManagerFindAllServiceGrpc.newBlockingStub(channel)
        }
    }
}