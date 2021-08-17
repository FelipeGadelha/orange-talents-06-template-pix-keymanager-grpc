package br.com.zup.felipe.gadelha.api.controller

import br.com.zup.felipe.gadelha.*
import br.com.zup.felipe.gadelha.PixKeyType.*
import br.com.zup.felipe.gadelha.domain.repository.PixRepository
import br.com.zup.felipe.gadelha.infra.client.ItauClient
import br.com.zup.felipe.gadelha.infra.client.ItauClientRs
import com.github.javafaker.Faker
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito
import java.util.*
import java.util.stream.Stream
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class KeyManagerRegisterControllerTest(
    private val repository: PixRepository,
    private val grpcClient: KeyManagerRegisterServiceGrpc.KeyManagerRegisterServiceBlockingStub,
    private val itauClient: ItauClient
) {
    private val faker: Faker = Faker(Locale("pt-BR"))

    @MockBean(ItauClient::class)
    fun itauMock(): ItauClient = Mockito.mock(ItauClient::class.java)

    @BeforeEach
    internal fun setup() {
        repository.deleteAll()
    }

    companion object {
        val faker: Faker = Faker(Locale("pt-BR"))

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
                Arguments.of(CEL_PHONE.toString(), "+5511${Random().nextInt(999999999).toString()}"),
                Arguments.of(EMAIL.toString(), faker.internet().emailAddress()),
                Arguments.of(RANDOM_KEY.toString(), ""),
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
                Arguments.of(CEL_PHONE.toString(), faker.phoneNumber().cellPhone()),
                Arguments.of(CEL_PHONE.toString(), "+55${faker.phoneNumber().cellPhone()}"),
                Arguments.of(RANDOM_KEY.toString(), "+55${faker.phoneNumber().cellPhone()}"),
                Arguments.of(RANDOM_KEY.toString(), "${faker.internet().emailAddress()}"),
                Arguments.of(RANDOM_KEY.toString(), "58272986035"),
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
        val itauClientRs = ItauClientRs(request.clientId)
        Mockito.`when`(itauClient.findClient(request.clientId)).thenReturn(HttpResponse.ok(itauClientRs))
        var response: PixRs = grpcClient.register(request)
        with(response) {
            assertNotNull(pixId)
            assertNotNull(repository.findById(UUID.fromString(pixId)))
            assertEquals(repository.findById(UUID.fromString(pixId)).get().keyType, request.keyType)
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
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals(
                "INVALID_ARGUMENT: O valor de chave '$invalid' é invalido para o tipo de chave '${keyType}'",
                this.message
            )
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
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals(errorMessage, message
            )
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
        var exception = assertThrows<StatusRuntimeException> { grpcClient.register(request) }
        with(exception) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("INVALID_ARGUMENT: O valor da chave não pode passar de 77 caracteres", this.message)
        }
    }

    @Test
    internal fun `should not register pix when account is invalid`() {
        val request = PixRq.newBuilder()
            .setClientId("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .setValue("felipe@email.com")
            .setKeyType(PixKeyType.EMAIL)
            .build()
        var exception = assertThrows<StatusRuntimeException> { grpcClient.register(request) }
        with(exception) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("INVALID_ARGUMENT: O tipo de conta é irreconhecivel", this.message)
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
            assertEquals("INVALID_ARGUMENT: O tipo de chave é irreconhecivel", this.message)
        }
    }

    @Test
    internal fun `should not register pix when key with repeated value`() {
        val request = PixRq.newBuilder()
            .setClientId("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .setValue("felipe@email.com")
            .setKeyType(PixKeyType.EMAIL)
            .setAccountType(AccountType.CURRENT)
            .build()
        val itauClientRs = ItauClientRs(request.clientId)
        Mockito.`when`(itauClient.findClient(request.clientId)).thenReturn(HttpResponse.ok(itauClientRs))
        val firstResponse: PixRs = grpcClient.register(request)
        with(firstResponse) {
            assertNotNull(pixId)
            assertNotNull(repository.findById(UUID.fromString(pixId)))
            assertEquals(repository.findById(UUID.fromString(pixId)).get().keyType, request.keyType)
        }
        var exception = assertThrows<StatusRuntimeException> { grpcClient.register(request) }
        with(exception) {
            assertEquals(Status.ALREADY_EXISTS.code, status.code)
            assertEquals("ALREADY_EXISTS: Chave já cadastrada: ${request.value}", this.message)
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
        Mockito.`when`(itauClient.findClient(request.clientId)).thenReturn(HttpResponse.notFound())
        var exception = assertThrows<StatusRuntimeException> { grpcClient.register(request) }
        with(exception) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("NOT_FOUND: Cliente não cadastrado no Itaú", this.message)
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