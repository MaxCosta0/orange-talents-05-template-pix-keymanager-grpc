package br.com.zupacademy.maxley.pix.lista

import br.com.zupacademy.maxley.KeyManagerListaGrpcSErviceGrpc
import br.com.zupacademy.maxley.ListaChavePixRequest
import br.com.zupacademy.maxley.model.ChavePix
import br.com.zupacademy.maxley.model.ContaAssociada
import br.com.zupacademy.maxley.pix.TipoChavePix
import br.com.zupacademy.maxley.pix.TipoContaItau
import br.com.zupacademy.maxley.pix.consulta.ConsultaChavePixEndpointTest
import br.com.zupacademy.maxley.repository.ChavePixRepository
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

@MicronautTest(transactional = false)
internal class ListaChavePixEndpointTest(
    private val chavePixRepository: ChavePixRepository,
    private val grpcClient: KeyManagerListaGrpcSErviceGrpc.KeyManagerListaGrpcSErviceBlockingStub
) {
    companion object {
        val CLIENT_ID: UUID = UUID.randomUUID()
        const val CPF_VALIDO = "32059775051"
        var chaveCpf: ChavePix? = null
        var chaveCelular: ChavePix? = null
        var chaveEmail: ChavePix? = null
    }

    @BeforeEach
    fun setup() {
        chaveCpf = chavePixRepository.save(novaChave(CLIENT_ID, tipoDeChave = TipoChavePix.CPF, CPF_VALIDO))
        chaveCelular = chavePixRepository.save(novaChave(CLIENT_ID, tipoDeChave = TipoChavePix.CELULAR, chave = "+55900001111"))
        chaveEmail = chavePixRepository.save(novaChave(CLIENT_ID, tipoDeChave = TipoChavePix.EMAIL, chave = "maxley@mail.com"))
    }

    @AfterEach
    fun cleanup() {
        chavePixRepository.deleteAll()
    }

    @Test
    fun `deve listar todas as chaves pix de um cliente`() {
        //Cenario

        //Ação
        val response = grpcClient.lista(
            ListaChavePixRequest.newBuilder()
                .setClientId(CLIENT_ID.toString())
                .build()
        )

        //Validação
        with(response) {
            assertEquals(3, response.chavesPixList.size)
            assertEquals(chaveCpf?.chave, response.chavesPixList[0].chave)
            assertEquals(chaveCelular?.chave, response.chavesPixList[1].chave)
            assertEquals(chaveEmail?.chave, response.chavesPixList[2].chave)
        }
    }

    @Test
    fun `nao deve listar chave quando clientId esta nulo ou em branco`() {
        //Cenario

        //Ação
        val exception = assertThrows<StatusRuntimeException>{
            grpcClient.lista(
                ListaChavePixRequest.newBuilder().build()
            )
        }

        //Validação
        with(exception) {
            assertEquals(Status.INVALID_ARGUMENT.code, exception.status.code)
            assertEquals("clientId nao pode estar em branco ou nulo", exception.status.description)
        }
    }

    @Test
    fun `nao deve listar chave para cliente que nao possui chave`() {
        //Cenario
        val clienteSemChave = UUID.randomUUID().toString()

        //Ação
        val response = grpcClient.lista(
                ListaChavePixRequest.newBuilder()
                    .setClientId(clienteSemChave)
                    .build()
            )

        //validação
        with(response) {
            assertTrue(response.chavesPixList.isEmpty())
        }
    }

    @Factory
    class Clients {
        @Bean
        fun blockingStub(
            @GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel
        ): KeyManagerListaGrpcSErviceGrpc.KeyManagerListaGrpcSErviceBlockingStub? {
            return KeyManagerListaGrpcSErviceGrpc.newBlockingStub(channel)
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
                cpfDoTitular = ConsultaChavePixEndpointTest.CPF_VALIDO,
                agencia = "0001",
                numeroDaConta = "291900"
            )
        )
    }

}