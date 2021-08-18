package br.com.zup.felipe.gadelha.api.controller

import br.com.zup.felipe.gadelha.*
import br.com.zup.felipe.gadelha.domain.entity.Pix
import br.com.zup.felipe.gadelha.domain.entity.TypeAccount
import br.com.zup.felipe.gadelha.domain.entity.TypeKey
import br.com.zup.felipe.gadelha.domain.repository.PixRepository
import br.com.zup.felipe.gadelha.infra.client.ItauClient
import com.github.javafaker.Faker
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import java.util.*
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class KeyManagerDeleteControllerTest(
    private val repository: PixRepository,
    private val grpcClient: KeyManagerDeleteServiceGrpc.KeyManagerDeleteServiceBlockingStub,
) {

    private val faker: Faker = Faker(Locale("pt-BR"))

    @MockBean(ItauClient::class)
    fun itauMock(): ItauClient = Mockito.mock(ItauClient::class.java)

    private lateinit var pix: Pix


    @BeforeEach
    internal fun setup() {
        repository.deleteAll()
        pix = repository.save(Pix(
            clientId = UUID.fromString("c56dfef4-7901-44fb-84e2-a2cefb157890"),
            value = faker.internet().emailAddress(),
            keyType = TypeKey.EMAIL.toString(),
            accountType = TypeAccount.CURRENT.toString()
            )
        )
    }

    @Test
    internal fun `should delete pix key successfully`() {
        grpcClient.delete(DeletePixRq.newBuilder()
            .setPixId(pix.id.toString())
            .setClientId(pix.clientId.toString())
            .build()
        )
        assertTrue(repository.findAll().isEmpty())
    }

    @Test
    internal fun `should not delete pix key when user does not have permission`() {
        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.delete(
                DeletePixRq.newBuilder()
                    .setPixId(pix.id.toString())
                    .setClientId(UUID.randomUUID().toString())
                    .build()
            )
        }
        with(exception) {
            assertEquals(Status.PERMISSION_DENIED.code, status.code)
            assertEquals("Chave Pix não encontrado ou não pertence ao cliente", status.description)
            assertTrue(repository.findAll().isNotEmpty())
        }
    }
    @Test
    internal fun `should not delete pix key when clientId nulo`() {
        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.delete(DeletePixRq.newBuilder()
                .setPixId(pix.id.toString())
                .build()
            )
        }
        with(exception) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("O clientId não pode ser nulo", status.description)
            assertTrue(repository.findAll().isNotEmpty())
        }
    }
    @Test
    internal fun `should not delete pix key when pixId nulo`() {
        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.delete(DeletePixRq.newBuilder()
                .setClientId("c56dfef4-7901-44fb-84e2-a2cefb157890")
                .build()
            )
        }
        with(exception) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("O pixId não pode ser nulo", status.description)
            assertTrue(repository.findAll().isNotEmpty())
        }
    }

    @Test
    internal fun `should not delete pix key when clientId invalid`() {
        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.delete(DeletePixRq.newBuilder()
                .setPixId(pix.id.toString())
                .setClientId("c56dfef4-7901-44fb-84e2")
                .build()
            )
        }
        with(exception) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("ClientId inválido", status.description)
            assertTrue(repository.findAll().isNotEmpty())
        }
    }

    @Factory
    class DeleteClients {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeyManagerDeleteServiceGrpc.KeyManagerDeleteServiceBlockingStub {
            return KeyManagerDeleteServiceGrpc.newBlockingStub(channel)
        }
    }
}