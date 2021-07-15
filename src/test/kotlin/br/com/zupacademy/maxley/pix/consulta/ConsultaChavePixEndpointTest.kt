package br.com.zupacademy.maxley.pix.consulta

import br.com.zupacademy.maxley.ConsultaChavePixRequest
import br.com.zupacademy.maxley.KeyManagerConsultaGrpcServiceGrpc
import br.com.zupacademy.maxley.TipoDeChave
import br.com.zupacademy.maxley.integration.bcb.BancoCentralClient
import br.com.zupacademy.maxley.integration.bcb.dto.*
import br.com.zupacademy.maxley.model.ChavePix
import br.com.zupacademy.maxley.model.ContaAssociada
import br.com.zupacademy.maxley.pix.TipoChavePix
import br.com.zupacademy.maxley.pix.TipoContaItau
import br.com.zupacademy.maxley.repository.ChavePixRepository
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import java.lang.RuntimeException
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

@MicronautTest(transactional = false)
internal class ConsultaChavePixEndpointTest(
    private val chavePixRepository: ChavePixRepository,
    private val grpcClient: KeyManagerConsultaGrpcServiceGrpc.KeyManagerConsultaGrpcServiceBlockingStub
) {

    @Inject
    lateinit var bcbClient: BancoCentralClient

    companion object{
        val CLIENT_ID = UUID.randomUUID()
        const val CPF_VALIDO = "32059775051"
    }

    @BeforeEach
    fun setup() {
        chavePixRepository.save(novaChave(clientId = CLIENT_ID, tipoDeChave = TipoChavePix.CPF, chave = CPF_VALIDO))
    }

    @AfterEach
    fun cleanup() {
        chavePixRepository.deleteAll()
    }

    @Test
    fun `deve consultar chave por PixId quando pixId e clientId validos`() {
        //Cenario
        val chaveExistente = chavePixRepository.findByChave(CPF_VALIDO).get()

        //Ação
        val response = grpcClient.consulta(
            ConsultaChavePixRequest.newBuilder()
                .setPixId(
                    ConsultaChavePixRequest.FiltroPorPixId.newBuilder()
                        .setPixId(chaveExistente.id.toString())
                        .setClientId(CLIENT_ID.toString())
                        .build()
                )
                .build()
        )

        //Validação
        with(response) {
            assertEquals(CLIENT_ID.toString(), this.clienteId)
            assertEquals(chaveExistente.id.toString(), this.pixId)
            assertEquals(chaveExistente.chave, this.chave.chave)
            assertEquals(chaveExistente.tipoChavePix.name, this.chave.tipo.name)
        }
    }

    @Test
    fun `nao deve consultar chave por PixId quando filtro invalido`() {
        //Cenario

        //Ação
        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.consulta(
                ConsultaChavePixRequest.newBuilder()
                    .setPixId(
                        ConsultaChavePixRequest.FiltroPorPixId.newBuilder()
                            .setPixId("")
                            .setClientId("")
                            .build()
                    )
                    .build()
            )
        }

        //Validação
        with(exception) {
            assertEquals(Status.INVALID_ARGUMENT.code, this.status.code)
            assertEquals("Chave Pix invalida ou nao informada", this.status.description)
        }
    }

    @Test
    fun `nao deve consultar chave por PixId quando nao cadastrada localmente`() {
        //Cenario

        //Ação
        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.consulta(
                ConsultaChavePixRequest.newBuilder()
                    .setPixId(ConsultaChavePixRequest.FiltroPorPixId.newBuilder()
                        .setClientId(CLIENT_ID.toString())
                        .setPixId(UUID.randomUUID().toString())     //Chave Inexistente
                        .build()
                    )
                    .build()
            )
        }

        //Validação
        with(exception) {
            assertEquals(Status.NOT_FOUND.code, this.status.code)
            assertEquals("Chave Pix nao encontrada", this.status.description)
        }
    }

    @Test
    fun `deve consultar chave por valor da chave quando cadastrada localmente`() {
        //Cenario
        val chaveExistente = chavePixRepository.findByChave(CPF_VALIDO).get()

        //Ação
        val response = grpcClient.consulta(
            ConsultaChavePixRequest.newBuilder()
                .setChave(CPF_VALIDO)
                .build()
        )

        //Validação
        with(response) {
            assertEquals(CLIENT_ID.toString(), response.clienteId.toString())
            assertEquals(chaveExistente.id.toString(), response.pixId.toString())
            assertEquals(TipoDeChave.CPF.name, response.chave.tipo.name)
            assertEquals(CPF_VALIDO, response.chave.chave.toString())

        }
    }

    @Test
    fun `deve consultar chave quando chave nao cadastrada localmente mas cadastrada no BCB`() {
        //Cenario

        val chaveNaoCadastrada = novaChave(
            clientId = CLIENT_ID,
            tipoDeChave = TipoChavePix.EMAIL,
            chave = "maxley@email.com"
        )

        val bcbResponse = consultaChaveBcbResponse(chaveNaoCadastrada)

        Mockito.`when`(bcbClient.consultaChave(chaveNaoCadastrada.chave))
            .thenReturn(HttpResponse.ok(bcbResponse))

        //Ação
        val response = grpcClient.consulta(
            ConsultaChavePixRequest.newBuilder()
                .setChave(chaveNaoCadastrada.chave)
                .build()
        )

        //Validação
        with(response) {
            assertEquals("", this.clienteId)
            assertEquals("", this.pixId)
            assertEquals(TipoDeChave.EMAIL.name, response.chave.tipo.name)
            assertEquals(chaveNaoCadastrada.chave, response.chave.chave)
        }
    }

    @Test
    fun `nao deve consultar chave quando nao cadastrada localmente nem no BCB`() {
        //Cenario
        val chaveNaoCadastrada = novaChave(
            clientId = CLIENT_ID,
            tipoDeChave = TipoChavePix.ALEATORIA,
            chave = UUID.randomUUID().toString()
        )
        Mockito.`when`(bcbClient.consultaChave(chaveNaoCadastrada.chave))
            .thenReturn(HttpResponse.notFound())

        //Ação
        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.consulta(ConsultaChavePixRequest.newBuilder()
                .setChave(chaveNaoCadastrada.chave)
                .build()
            )
        }

        //Validação
        with(exception) {
            assertEquals(Status.NOT_FOUND.code, this.status.code)
            assertEquals("Chave Pix nao encontrada", this.status.description)
        }
    }

    @Test
    fun `nao deve consulta por valor da chave quando Filtro invalido`() {
        //Cenario

        //Ação
        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.consulta(
                ConsultaChavePixRequest.newBuilder()
                    .setChave("")           //Entrada de chave invalida
                    .build()
            )
        }

        //Validação
        with(exception) {
            assertEquals(Status.INVALID_ARGUMENT.code, this.status.code)
            assertEquals("Dados Invalidos", this.status.description)
        }
    }

    @Test
    fun `nao deve consultar chave quando nao for apresentado nenhum PixId ou chave`() {
        //Cenario

        //Ação
        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.consulta(
                ConsultaChavePixRequest.newBuilder().build()
            )
        }

        //Validação
        with(exception) {
            assertEquals(Status.INVALID_ARGUMENT.code, this.status.code)
            assertEquals("Chave Pix invalida ou nao informada", this.status.description)
        }
    }


    @MockBean(BancoCentralClient::class)
    fun bancoCentralClient(): BancoCentralClient? {
        return Mockito.mock(BancoCentralClient::class.java)
    }

    @Factory
    class Clients {
        @Bean
        fun blockingStub(
            @GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel
        ): KeyManagerConsultaGrpcServiceGrpc.KeyManagerConsultaGrpcServiceBlockingStub {
            return KeyManagerConsultaGrpcServiceGrpc.newBlockingStub(channel)
        }
    }

    private fun novaChave(clientId: UUID, tipoDeChave: TipoChavePix, chave: String): ChavePix {
        return ChavePix(
            clientId = clientId,
            tipoChavePix = tipoDeChave,
            chave = chave,
            tipoContaItau = TipoContaItau.CONTA_CORRENTE,
            conta = ContaAssociada(
                instituicao = "ITAU_UNIBANCO",
                nomeDoTitular = "Rafael M C Ponte",
                cpfDoTitular = CPF_VALIDO,
                agencia = "0001",
                numeroDaConta = "291900"
            )
        )
    }

    fun consultaChaveBcbResponse(chavePix: ChavePix): ConsultaChaveBcbResponse {
        return ConsultaChaveBcbResponse(
            keyType = when (chavePix.tipoChavePix) {
                TipoChavePix.EMAIL -> PixKeyTypeBcb.EMAIL
                TipoChavePix.CPF -> PixKeyTypeBcb.CPF
                TipoChavePix.CELULAR -> PixKeyTypeBcb.PHONE
                TipoChavePix.ALEATORIA -> PixKeyTypeBcb.RANDOM
                else -> PixKeyTypeBcb.UNKNOWN
            },
            key = chavePix.chave,
            bankAccount = BankAccountRequest(
                participant = chavePix.conta.ITAU_UNIBANCO_ISPB,
                branch = chavePix.conta.agencia,
                accountNumber = chavePix.conta.numeroDaConta,
                accountType = when (chavePix.tipoContaItau) {
                    TipoContaItau.CONTA_CORRENTE -> AccountType.CACC
                    TipoContaItau.CONTA_POUPANCA -> AccountType.SVGS
                    else -> AccountType.UNKNOWN
                }
            ),
            owner = OwnerRequest(
                type = TypePerson.NATURAL_PERSON,
                name = chavePix.conta.nomeDoTitular,
                taxIdNumber = chavePix.conta.cpfDoTitular
            ),
            createdAt = LocalDateTime.now()
        )
    }
}