package br.com.zupacademy.maxley.pix.registra

import br.com.zupacademy.maxley.KeyManagerServiceGrpc
import br.com.zupacademy.maxley.RegistraChavePixRequest
import br.com.zupacademy.maxley.TipoDeChave
import br.com.zupacademy.maxley.TipoDeConta
import br.com.zupacademy.maxley.conta.dto.ContaResponse
import br.com.zupacademy.maxley.conta.dto.InstituicaoResponse
import br.com.zupacademy.maxley.conta.dto.TitularResponse
import br.com.zupacademy.maxley.integration.bcb.BancoCentralClient
import br.com.zupacademy.maxley.integration.bcb.dto.*
import br.com.zupacademy.maxley.integration.itau.ItauContasClient
import br.com.zupacademy.maxley.pix.TipoChavePix
import br.com.zupacademy.maxley.pix.TipoContaItau
import br.com.zupacademy.maxley.model.ChavePix
import br.com.zupacademy.maxley.model.ContaAssociada
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
import org.junit.Assert
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

@MicronautTest(transactional = false)
internal class RegistraChaveEndpointTest(
    private val chavePixRepository: ChavePixRepository,
    private val grpcClient: KeyManagerServiceGrpc.KeyManagerServiceBlockingStub){

    @Inject
    lateinit var itauContasClient: ItauContasClient

    @Inject
    lateinit var bcbclient: BancoCentralClient

    companion object {
        val CLIENT_ID: UUID = UUID.randomUUID()
        const val CPF_VALIDO: String = "79652671010"
        const val CELULAR = "+5585988714077"
    }

    @BeforeEach
    fun setup(){
        chavePixRepository.deleteAll()
    }

    @Test
    fun `deve registrar uma nova chave pix`() {
        //cenario

        val contaResponse = dadosDaContaResponse()

        Mockito.`when`(itauContasClient.buscaContaPorTipo(
            clienteId = CLIENT_ID.toString(),
            tipo = TipoContaItau.CONTA_CORRENTE)
        ).thenReturn(HttpResponse.ok(contaResponse))

        Mockito.`when`(bcbclient.registraChavePix(
            ChavePixBcbRequest(
                keyType = PixKeyTypeBcb.EMAIL,
                key = "maxleysoares@gmail.com",
                bankAccount = BankAccountRequest(
                    participant = contaResponse.instituicao.ispb,
                    branch = contaResponse.agencia,
                    accountNumber = contaResponse.numero,
                    accountType = AccountType.CACC
                ),
                owner = OwnerRequest(
                    type = TypePerson.NATURAL_PERSON,
                    name = contaResponse.titular.nome,
                    taxIdNumber = contaResponse.titular.cpf
                )
            )
        )).thenReturn(HttpResponse.created(
            ChavePixBcbResponse(
                keyType = PixKeyTypeBcb.EMAIL,
                key = "maxleysoares@gmail.com",
                bankAccount = BankAccountRequest(
                    participant = contaResponse.instituicao.ispb,
                    branch = contaResponse.agencia,
                    accountNumber = contaResponse.numero,
                    accountType = AccountType.CACC
                ),
                owner = OwnerRequest(
                    type = TypePerson.NATURAL_PERSON,
                    name = contaResponse.titular.nome,
                    taxIdNumber = contaResponse.titular.cpf
                ),
                createdAt = LocalDateTime.now()
            )
        ))

        //ação
        val response = grpcClient.registra(
            RegistraChavePixRequest.newBuilder()
                .setClientId(CLIENT_ID.toString())
                .setTipoDeChave(TipoDeChave.EMAIL)
                .setChave("maxleysoares@gmail.com")
                .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
                .build()
        )

        //validação
        with(response){
            Assert.assertEquals(CLIENT_ID.toString(), clienteId)
            Assert.assertNotNull(pixId)
        }
    }

    @Test
    fun `nao deve registrar chave ja existente`(){
        //cenario
        val chave = ChavePix(
            clientId = CLIENT_ID,
            tipoChavePix = TipoChavePix.CPF,
            chave = CPF_VALIDO,
            tipoContaItau = TipoContaItau.CONTA_CORRENTE,
            conta = ContaAssociada(
                instituicao = "ITAÚ UNIBANCO S.A.",
                nomeDoTitular = "Rafael M C Ponte",
                cpfDoTitular = "02467781054",
                agencia = "0001",
                numeroDaConta = "291900"
            )
        )

        chavePixRepository.save(chave)

        //ação
        val exception = assertThrows<StatusRuntimeException>{
            grpcClient.registra(
                RegistraChavePixRequest.newBuilder()
                    .setClientId(CLIENT_ID.toString())
                    .setTipoDeChave(TipoDeChave.CPF)
                    .setChave(CPF_VALIDO)
                    .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
                    .build()
            )
        }

        //validação
        with(exception) {
            assertEquals(Status.ALREADY_EXISTS.code, status.code)
            assertEquals("Chave pix '${CPF_VALIDO}' ja existe", status.description )
        }

    }

    @Test
    fun `nao deve registrar chave para conta inexistente no sistema ERP ITAU`(){
        //cenario
        Mockito.`when`(itauContasClient.buscaContaPorTipo(
            clienteId = CLIENT_ID.toString(),
            tipo = TipoContaItau.CONTA_CORRENTE
        )).thenReturn(HttpResponse.notFound())

        //ação
        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.registra(
                RegistraChavePixRequest.newBuilder()
                    .setClientId(CLIENT_ID.toString())
                    .setTipoDeChave(TipoDeChave.CELULAR)
                    .setChave(CELULAR)
                    .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
                    .build()
            )
        }

        //validação
        with(exception) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("ClienteId '${CLIENT_ID}' nao encontrado", status.description)
        }
    }

    @Test
    fun `nao deve registrar chave quando parametros forem invalidos`() {
        //cenario

        //ação
        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.registra(
                RegistraChavePixRequest.newBuilder()
                    .build()
            )
        }

        //validação
        with(exception) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Dados Invalidos", status.description)
//            assertNotNull(status.description)
        }
    }

    @Test
    fun `nao deve registrar chave quando nao for possivel registrar no Banco Central`() {
        //Cenario
        val contaResponse = dadosDaContaResponse()

        Mockito.`when`(itauContasClient.buscaContaPorTipo(
            clienteId = CLIENT_ID.toString(),
            tipo = TipoContaItau.CONTA_CORRENTE)
        ).thenReturn(HttpResponse.ok(contaResponse))

        Mockito.`when`(bcbclient.registraChavePix(
            ChavePixBcbRequest(
                keyType = PixKeyTypeBcb.EMAIL,
                key = "maxleysoares@gmail.com",
                bankAccount = BankAccountRequest(
                    participant = contaResponse.instituicao.ispb,
                    branch = contaResponse.agencia,
                    accountNumber = contaResponse.numero,
                    accountType = AccountType.CACC
                ),
                owner = OwnerRequest(
                    type = TypePerson.NATURAL_PERSON,
                    name = contaResponse.titular.nome,
                    taxIdNumber = contaResponse.titular.cpf
                )
            )
        )).thenReturn(HttpResponse.unprocessableEntity())

        //Ação
        val exception = assertThrows<StatusRuntimeException>{
            grpcClient.registra(
                RegistraChavePixRequest.newBuilder()
                    .setClientId(CLIENT_ID.toString())
                    .setTipoDeChave(TipoDeChave.EMAIL)
                    .setChave("maxleysoares@gmail.com")
                    .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
                    .build()
            )
        }

        //Validação
        with(exception) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals(
                "Nao foi possivel registrar chave pix 'maxleysoares@gmail.com' no Banco Central",
                status.description
            )
        }

    }

    @MockBean(BancoCentralClient::class)
    fun bancoCentralClient(): BancoCentralClient? {
        return Mockito.mock(BancoCentralClient::class.java)
    }

    @MockBean(ItauContasClient::class)
    fun itauClient(): ItauContasClient? {
        return Mockito.mock(ItauContasClient::class.java)
    }

    @Factory
    class Clients {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel)
        :KeyManagerServiceGrpc.KeyManagerServiceBlockingStub{
            return KeyManagerServiceGrpc.newBlockingStub(channel)
        }

    }

    private fun dadosDaContaResponse(): ContaResponse {
        return ContaResponse(
            tipo = "CONTA_CORRENTE",
            agencia = "0001",
            numero = "291900",
            instituicao = InstituicaoResponse( "ITAÚ UNIBANCO S.A.", ispb = "60701190"),
            titular = TitularResponse(
                id = UUID.fromString("c56dfef4-7901-44fb-84e2-a2cefb157890"),
                nome = "Rafael M C Ponte",
                cpf = "02467781054"
            )
        )
    }

}