package br.com.zup.felipe.gadelha.api.controller

import br.com.zup.felipe.gadelha.*
import br.com.zup.felipe.gadelha.PixKeyType.*
import br.com.zup.felipe.gadelha.domain.entity.Pix
import br.com.zup.felipe.gadelha.domain.entity.TypeAccount
import br.com.zup.felipe.gadelha.domain.entity.TypeKey
import br.com.zup.felipe.gadelha.domain.repository.PixRepository
import br.com.zup.felipe.gadelha.infra.client.BCBClient
import br.com.zup.felipe.gadelha.infra.client.ItauClient
import br.com.zup.felipe.gadelha.infra.dto.request.BCBBankAccount
import br.com.zup.felipe.gadelha.infra.dto.request.BCBCreatePixKeyRq
import br.com.zup.felipe.gadelha.infra.dto.request.BCBOwner
import br.com.zup.felipe.gadelha.infra.dto.response.AccountItauRs
import br.com.zup.felipe.gadelha.infra.dto.response.BCBCreatePixKeyRs
import br.com.zup.felipe.gadelha.infra.dto.response.HolderItauRs
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
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.util.*
import java.util.stream.Stream
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class KeyManagerRegisterControllerTest(
    private val grpcClient: KeyManagerRegisterServiceGrpc.KeyManagerRegisterServiceBlockingStub,
    private val repository: PixRepository,
    val itauClient: ItauClient,
    val bcbClient: BCBClient
) {

    private val faker: Faker = Faker(Locale("pt-BR"))

    @MockBean(ItauClient::class)
    fun itauMock(): ItauClient = mock(ItauClient::class.java)

    @MockBean(BCBClient::class)
    fun bcbMock(): BCBClient = mock(BCBClient::class.java)

    private val account = AccountItauRs(
        type = "CONTA_CORRENTE",
        agency = "0001",
        number = "291900",
        institution = InstitutionItauRs(
            name = "ITAÚ UNIBANCO S.A.",
            ispb = "60701190"
        ),
        holder = HolderItauRs(
            id = "c56dfef4-7901-44fb-84e2-a2cefb157890",
            name = "Rafael M C Ponte",
            cpf = "02467781054"
        )
    )

    private fun createPixRq(keyType: String, key: String) = BCBCreatePixKeyRq(
        keyType = keyType,
        key = key,
        owner = BCBOwner(
            name = account.holder.name,
            type = "NATURAL_PERSON",
            taxIdNumber = account.holder.cpf
        ),
        bankAccount = BCBBankAccount(
            participant = account.holder.id,
            branch = account.agency,
            accountNumber = account.number,
            accountType = if (account.type == "CONTA_CORRENTE") "CACC" else "SVGS"
        )
    )

    private fun createPixRs(keyType: String, key: String) = BCBCreatePixKeyRs(
        keyType = keyType,
        key = key,
        createdAt = LocalDateTime.now().toString()
    )
    private fun convertAccount(request: PixRq): String =
        if (request.accountType == AccountType.CURRENT) "CONTA_CORRENTE" else "CONTA_POUPANCA"

    @BeforeEach
    internal fun setup() {
        repository.deleteAll()
    }

    companion object {
        private val faker: Faker = Faker(Locale("pt-BR"))

        @JvmStatic
        fun provideTestArgumentsSuccessfully(): Stream<Arguments> =
            Stream.of(
                Arguments.of(CPF.toString(), "04537189495"),
                Arguments.of(CPF.toString(), "11343733090"),
                Arguments.of(CPF.toString(), "25296732251"),
                Arguments.of(CPF.toString(), "31335427708"),
                Arguments.of(CPF.toString(), "44778335554"),
                Arguments.of(CPF.toString(), "51615666710"),
                Arguments.of(CPF.toString(), "69458164516"),
                Arguments.of(CPF.toString(), "73281228119"),
                Arguments.of(CPF.toString(), "82372374710"),
                Arguments.of(CPF.toString(), "91611511496"),
                Arguments.of(PHONE.toString(), "+5511${Random().nextInt(999999999)}"),
                Arguments.of(EMAIL.toString(), faker.internet().emailAddress()),
                Arguments.of(RANDOM.toString(), ""),
            )

        @JvmStatic
        fun provideTestArgumentsInvalidKeysFailure(): Stream<Arguments> =
            Stream.of(
                Arguments.of(CPF.toString(), "123-123-765-45"),
                Arguments.of(EMAIL.toString(), "${faker.name().firstName()}@"),
                Arguments.of(EMAIL.toString(), "${faker.name().firstName()}@email"),
                Arguments.of(EMAIL.toString(), "${faker.name().firstName()}@@gmail"),
                Arguments.of(EMAIL.toString(), "${faker.name().firstName()}@gmail@com.com"),
                Arguments.of(EMAIL.toString(), "${faker.name().firstName()}@outlook@com"),
                Arguments.of(PHONE.toString(), faker.phoneNumber().cellPhone()),
                Arguments.of(PHONE.toString(), "+55${faker.phoneNumber().cellPhone()}"),
//                Arguments.of(RANDOM.toString(), "+55${faker.phoneNumber().cellPhone()}"),
//                Arguments.of(RANDOM.toString(), "${faker.internet().emailAddress()}"),
//                Arguments.of(RANDOM.toString(), "58272986035"),
            )

        @JvmStatic
        fun provideTestArgumentsInvalidClientId(): Stream<Arguments> =
            Stream.of(
                Arguments.of("", "INVALID_ARGUMENT: O clientId não pode ser nulo"),
                Arguments.of("c56dfef4-7901-44fb-84e2-a2cefb1", "INVALID_ARGUMENT: O clientId inválido"),
            )
    }

    @ParameterizedTest
    @MethodSource("provideTestArgumentsSuccessfully")
    internal fun `should register pix keys successfully`(keyType: String, value: String) {
        val request = PixRq.newBuilder()
            .setClientId("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .setValue(value)
            .setKeyType(PixKeyType.valueOf(keyType))
            .setAccountType(AccountType.CURRENT)
            .build()
        `when`(itauClient.findAccountClient(request.clientId.toString(), convertAccount(request)))
            .thenReturn(HttpResponse.ok(account))
        `when`(bcbClient.registerPix(createPixRq(request.keyType.name, request.value)))
            .thenReturn(HttpResponse.created(createPixRs(request.keyType.name, request.value)))
        var response: PixRs = grpcClient.register(request)
        with(response) {
            assertNotNull(pixId)
            assertNotNull(repository.findById(UUID.fromString(pixId)))
            assertEquals(repository.findById(UUID.fromString(pixId)).get().typeKey.name, request.keyType.name)
        }
    }

    @ParameterizedTest
    @MethodSource("provideTestArgumentsInvalidKeysFailure")
    internal fun `should not register pix when keys invalids`(keyType: String, invalid: String) {
        val request = PixRq.newBuilder()
            .setClientId("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .setValue(invalid)
            .setKeyType(PixKeyType.valueOf(keyType))
            .setAccountType(AccountType.CURRENT)
            .build()
        var exception = assertThrows<StatusRuntimeException> { grpcClient.register(request) }
        with(exception) {
            assertEquals(
                listOf(Pair("?? key ??", "A Chave $invalid é inválida para o tipo $keyType")),
                StatusProto.fromThrowable(this).let { status ->
                    val badRequest = status!!.detailsList[0].unpack(BadRequest::class.java)
                    badRequest.fieldViolationsList.map { it.field to it.description }
                }
            )
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("INVALID_ARGUMENT: Dados inválidos", this.message)
            assertTrue(repository.findAll().isEmpty())
        }
    }

    @ParameterizedTest
    @MethodSource("provideTestArgumentsInvalidClientId")
    internal fun `should not register pix when clientId invalid`(invalid: String, errorMessage: String) {
        val request = PixRq.newBuilder()
            .setClientId(invalid)
            .setValue("felipe@email.com")
            .setKeyType(PixKeyType.EMAIL)
            .setAccountType(AccountType.CURRENT)
            .build()
        var exception = assertThrows<StatusRuntimeException> { grpcClient.register(request) }
        with(exception) {
            assertEquals(
                listOf(Pair("uuid", "deve corresponder a \"[a-f0-9]{8}(-[a-f0-9]{4}){4}[a-f0-9]{8}\$\"")),
                StatusProto.fromThrowable(this).let { status ->
                    val badRequest = status!!.detailsList[0].unpack(BadRequest::class.java)
                    badRequest.fieldViolationsList.map { it.field to it.description }
                }
            )
            assertEquals(Status.INVALID_ARGUMENT.code.name, exception.status.code.name)
            assertEquals("Dados inválidos", exception.status.description)
            assertTrue(repository.findAll().isEmpty())
        }
    }

    @Test
    internal fun `should not register pix when key is bigger than allowed`() {
        val request = PixRq.newBuilder()
            .setClientId("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .setValue("invalid".repeat(11) + "@email.com")
            .setKeyType(PixKeyType.EMAIL)
            .setAccountType(AccountType.CURRENT)
            .build()
        val exception = assertThrows<StatusRuntimeException> { grpcClient.register(request) }
        with(exception) {
            assertEquals(Status.INVALID_ARGUMENT.code.name, exception.status.code.name)
            assertEquals("Dados inválidos", exception.status.description)
            assertTrue(repository.findAll().isEmpty())
            assertEquals(
                listOf(Pair("key", "tamanho deve ser entre 0 e 77")),
                StatusProto.fromThrowable(this).let { status ->
                    val badRequest = status!!.detailsList[0].unpack(BadRequest::class.java)
                    badRequest.fieldViolationsList.map { it.field to it.description }
                }
            )
        }
    }
    @Test
    internal fun `should not register pix when account is invalid`() {
        val request = PixRq.newBuilder()
            .setClientId("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .setValue("42609548344")
            .setKeyType(PixKeyType.EMAIL)
            .build()
        var exception = assertThrows<StatusRuntimeException> { grpcClient.register(request) }
        with(exception) {
            assertEquals(Status.INVALID_ARGUMENT.code.name, status.code.name)
            assertEquals("Dados inválidos", exception.status.description)
            assertTrue(repository.findAll().isEmpty())
            assertEquals(
                listOf(Pair("account", "O tipo de conta é irreconhecivel")),
                StatusProto.fromThrowable(this).let { status ->
                    val badRequest = status!!.detailsList[0].unpack(BadRequest::class.java)
                    badRequest.fieldViolationsList.map { it.field to it.description }
                }
            )
        }
    }

    @Test
    internal fun `should not register pix when keyType is invalid`() {
        val request = PixRq.newBuilder()
            .setClientId("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .setValue("felipe@email.com")
            .setAccountType(AccountType.CURRENT)
            .build()
        var exception = assertThrows<StatusRuntimeException> { grpcClient.register(request) }
        with(exception) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Dados inválidos", exception.status.description)
            assertTrue(repository.findAll().isEmpty())
            assertEquals(
                listOf(Pair("typeKey", "O tipo de chave é irreconhecivel")),
                StatusProto.fromThrowable(this).let { status ->
                    val badRequest = status!!.detailsList[0].unpack(BadRequest::class.java)
                    badRequest.fieldViolationsList.map { it.field to it.description }
                }
            )
        }
    }

    @Test
    internal fun `should not register pix when key with repeated value`() {
        val pixTest = Pix(
            clientId = UUID.fromString("c56dfef4-7901-44fb-84e2-a2cefb157890"),
            value = "felipe@email.com",
            typeKey = TypeKey.EMAIL,
            typeAccount = TypeAccount.SAVING,
            participant = "60701190",
            createdAt = LocalDateTime.now()
        )
        repository.save(pixTest)
        val request = PixRq.newBuilder()
            .setClientId("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .setValue("felipe@email.com")
            .setKeyType(PixKeyType.EMAIL)
            .setAccountType(AccountType.CURRENT)
            .build()
        `when`(itauClient.findAccountClient(request.clientId.toString(), convertAccount(request)))
            .thenReturn(HttpResponse.ok(account))
        `when`(bcbClient.registerPix(createPixRq(request.keyType.name, request.value)))
            .thenReturn(HttpResponse.created(createPixRs(request.keyType.name, request.value)))

        var exception = assertThrows<StatusRuntimeException> { grpcClient.register(request) }
        with(exception) {
            assertEquals(Status.ALREADY_EXISTS.code, status.code)
            assertEquals("ALREADY_EXISTS: Chave já cadastrada: ${request.value}", this.message)
            assertEquals(repository.findAll().size, 1)
        }
    }
    @Test
    internal fun `should not register pix when the customer is not registered with Itau`() {
        val request = PixRq.newBuilder()
            .setClientId("c56dfef4-7901-44fb-84e2-a2cefb157880")
            .setValue("felipe@email.com")
            .setKeyType(PixKeyType.EMAIL)
            .setAccountType(AccountType.CURRENT)
            .build()
        `when`(itauClient.findAccountClient(request.clientId.toString(), convertAccount(request)))
            .thenReturn(HttpResponse.notFound())

        var exception = assertThrows<StatusRuntimeException> { grpcClient.register(request) }
        with(exception) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("NOT_FOUND: Cliente não cadastrado no Itaú", this.message)
            assertTrue(repository.findAll().isEmpty())
        }
    }

    @Factory
    class RegisterClients {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeyManagerRegisterServiceGrpc.KeyManagerRegisterServiceBlockingStub {
            return KeyManagerRegisterServiceGrpc.newBlockingStub(channel)
        }
    }
}