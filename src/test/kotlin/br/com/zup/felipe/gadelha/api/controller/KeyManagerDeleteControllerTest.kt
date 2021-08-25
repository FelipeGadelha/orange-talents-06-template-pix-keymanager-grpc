package br.com.zup.felipe.gadelha.api.controller

import br.com.zup.felipe.gadelha.DeletePixRq
import br.com.zup.felipe.gadelha.KeyManagerDeleteServiceGrpc
import br.com.zup.felipe.gadelha.domain.entity.Pix
import br.com.zup.felipe.gadelha.domain.entity.TypeAccount
import br.com.zup.felipe.gadelha.domain.entity.TypeKey
import br.com.zup.felipe.gadelha.domain.repository.PixRepository
import br.com.zup.felipe.gadelha.infra.client.BCBClient
import br.com.zup.felipe.gadelha.infra.client.ItauClient
import br.com.zup.felipe.gadelha.infra.dto.request.BCBDeletePixKeyRq
import br.com.zup.felipe.gadelha.infra.dto.response.BCBDeletePixKeyRs
import br.com.zup.felipe.gadelha.infra.dto.response.ClientItauRs
import br.com.zup.felipe.gadelha.infra.dto.response.InstitutionItauRs
import com.github.javafaker.Faker
import com.google.rpc.BadRequest
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.protobuf.StatusProto
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.time.OffsetDateTime
import java.util.*
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class KeyManagerDeleteControllerTest(
    private val grpcClient: KeyManagerDeleteServiceGrpc.KeyManagerDeleteServiceBlockingStub,
    private val repository: PixRepository,
    private val bcbClient: BCBClient
) {

    @MockBean(ItauClient::class)
    fun itauMock(): ItauClient = mock(ItauClient::class.java)

    @MockBean(BCBClient::class)
    fun bcbMock(): BCBClient = mock(BCBClient::class.java)

    private val faker: Faker = Faker(Locale("pt-BR"))

    private lateinit var pix: Pix

    private var clientItauRs = ClientItauRs(
    id = "c56dfef4-7901-44fb-84e2-a2cefb157890",
    cpf = "02467781054",
    name = "Rafael M C Ponte",
    institution = InstitutionItauRs(
            name = "ITAÚ UNIBANCO S.A.",
            ispb = "60701190"
        ),
    )
    private fun deletePixRs(key: String, participant: String) = BCBDeletePixKeyRs(
        key = key,
        participant = participant,
        deletedAt = OffsetDateTime.now().toString()
    )

    @BeforeEach
    internal fun setup() {
        repository.deleteAll()
        pix = repository.save(Pix(
                clientId = UUID.fromString("c56dfef4-7901-44fb-84e2-a2cefb157890"),
                value = faker.internet().emailAddress().toString(),
                typeKey = TypeKey.EMAIL,
                typeAccount = TypeAccount.CURRENT,
                participant = "60701190"
            )
        )
    }

    @Test
    internal fun `should delete pix key successfully`() {
        `when`(bcbClient.deletePix(key = pix.value, request = BCBDeletePixKeyRq(
            key = pix.value,
            participant = pix.participant!!,
        ))).thenReturn(HttpResponse.ok(
            deletePixRs(key = pix.value, participant = pix.participant!!)))

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
            assertTrue(repository.findAll().isNotEmpty())
            assertEquals(
                "PERMISSION_DENIED: Chave Pix não encontrado ou não pertence ao cliente",
                this.message)
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
            assertEquals("Dados inválidos", status.description)
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
            assertEquals("Dados inválidos", status.description)
            assertTrue(repository.findAll().isNotEmpty())
            assertEquals(
                listOf(Pair("key", "deve corresponder a \"[a-f0-9]{8}(-[a-f0-9]{4}){4}[a-f0-9]{8}\$\"")),
                StatusProto.fromThrowable(this).let { status ->
                    val badRequest = status!!.detailsList[0].unpack(BadRequest::class.java)
                    badRequest.fieldViolationsList.map { it.field to it.description }
                }
            )
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
            assertTrue(repository.findAll().isNotEmpty())
            assertEquals(
                listOf(Pair("client", "deve corresponder a \"[a-f0-9]{8}(-[a-f0-9]{4}){4}[a-f0-9]{8}\$\"")),
                StatusProto.fromThrowable(this).let { status ->
                    val badRequest = status!!.detailsList[0].unpack(BadRequest::class.java)
                    badRequest.fieldViolationsList.map { it.field to it.description }
                }
            )
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