package br.com.zup.felipe.gadelha.api.controller

import br.com.zup.felipe.gadelha.FindPixRq
import br.com.zup.felipe.gadelha.KeyManagerFindServiceGrpc
import br.com.zup.felipe.gadelha.PixKeyType
import br.com.zup.felipe.gadelha.domain.entity.Pix
import br.com.zup.felipe.gadelha.domain.entity.TypeAccount
import br.com.zup.felipe.gadelha.domain.entity.TypeKey
import br.com.zup.felipe.gadelha.domain.repository.PixRepository
import br.com.zup.felipe.gadelha.domain.util.Institution
import br.com.zup.felipe.gadelha.infra.client.BCBClient
import br.com.zup.felipe.gadelha.infra.dto.response.AccountType
import br.com.zup.felipe.gadelha.infra.dto.response.BCBDetailsPixKeyRs
import br.com.zup.felipe.gadelha.infra.dto.response.BankAccountRs
import br.com.zup.felipe.gadelha.infra.dto.response.OnwerRs
import com.github.javafaker.Faker
import io.grpc.ManagedChannel
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.hamcrest.MatcherAssert
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.provider.Arguments
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.time.LocalDateTime
import java.util.*
import java.util.stream.Stream
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class KeyManagerFindControllerTest(
    private val grpcClient: KeyManagerFindServiceGrpc.KeyManagerFindServiceBlockingStub,
    private val repository: PixRepository,
    private val bcbClient: BCBClient
){
    private val faker: Faker = Faker(Locale("pt-BR"))

    @MockBean(BCBClient::class)
    fun bcbMock(): BCBClient = mock(BCBClient::class.java)

    private lateinit var pix: Pix

    private fun bcbDetailsRs(pix: Pix) = BCBDetailsPixKeyRs(
        keyType = pix.typeKey.toString(),
        key = pix.value,
        bankAccount = BankAccountRs(
            participant = pix.participant.toString(),
            branch = "0001",
            accountNumber = "291900",
            accountType = AccountType.CACC
        ),
        owner = OnwerRs(
            type = "",
            name = "Rafael M C Ponte",
            taxIdNumber = "02467781054"
        ),
        createdAt = LocalDateTime.now()
    )

    @BeforeEach
    internal fun setup() {
        repository.deleteAll()
        pix = repository.save(Pix(
                clientId = UUID.fromString("c56dfef4-7901-44fb-84e2-a2cefb157890"),
                value = faker.internet().emailAddress(),
                typeKey = TypeKey.EMAIL,
                typeAccount = TypeAccount.SAVING,
                participant = "60701190"
            )
        )
    }
    @Test
    internal fun `should find pix by pixId and clientId successfully`() {
        val request = FindPixRq.newBuilder()
            .setPixId(FindPixRq.FindByPixId.newBuilder()
                .setPixId(pix.id.toString())
                .setClientId(pix.clientId.toString())
            ).build()
        `when`(bcbClient.findPix(pix.value)).thenReturn(HttpResponse.ok(bcbDetailsRs(pix)))
        val result = grpcClient.find(request)
        with(result) {
            assertNotNull(this)
            assertEquals(request.pixId.clientId, this.clientId)
            assertEquals(request.pixId.pixId, this.pixId)
        }
    }

    @Test
    internal fun `should find pix by pixKey successfully`() {
        val request = FindPixRq.newBuilder()
                .setPixKey(pix.value)
                .build()
        val detailsRs = bcbDetailsRs(pix)
        `when`(bcbClient.findPix(pix.value)).thenReturn(HttpResponse.ok(detailsRs))
        val result = grpcClient.find(request)
        with(result) {
            assertNotNull(this)
            assertTrue(this.clientId.isBlank())
            assertTrue(this.pixId.isBlank())
            assertEquals(detailsRs.keyType, this.keyType)
            assertEquals(detailsRs.key, this.value)
            assertEquals(Institution.name(detailsRs.bankAccount.participant), this.account.name)
            assertEquals(detailsRs.bankAccount.branch, this.account.agency)
            assertEquals(detailsRs.bankAccount.accountNumber, this.account.number)
            assertEquals(detailsRs.bankAccount.accountType, this.account.type)
            assertEquals(detailsRs.owner.name, this.name)
            assertEquals(detailsRs.owner.taxIdNumber, this.cpf)
            assertEquals(detailsRs.createdAt.second, this.createdAt.seconds)
            assertEquals(detailsRs.createdAt.nano, this.createdAt.nanos)
        }
    }

    @Factory
    class FindClients {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeyManagerFindServiceGrpc.KeyManagerFindServiceBlockingStub {
            return KeyManagerFindServiceGrpc.newBlockingStub(channel)
        }
    }
}