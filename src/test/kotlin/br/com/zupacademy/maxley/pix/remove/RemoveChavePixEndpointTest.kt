package br.com.zupacademy.maxley.pix.remove

import br.com.zupacademy.maxley.KeyManagerRemoveGrpcServiceGrpc
import br.com.zupacademy.maxley.RemoveChavePixRequest
import br.com.zupacademy.maxley.integration.bcb.BancoCentralClient
import br.com.zupacademy.maxley.integration.bcb.dto.DeletePixKeyRequest
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
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import java.util.*
import javax.inject.Inject

@MicronautTest(transactional = false)
internal class RemoveChavePixEndpointTest(
    private val chavePixRepository: ChavePixRepository,
    private val grpcClient: KeyManagerRemoveGrpcServiceGrpc.KeyManagerRemoveGrpcServiceBlockingStub
) {

    @Inject
    lateinit var bcbClient: BancoCentralClient

    companion object{
        private val CLIENT_ID: UUID = UUID.randomUUID()
    }

    @BeforeEach
    fun setup() {
        chavePixRepository.deleteAll()
    }

    @AfterEach
    fun cleanup() {
        chavePixRepository.deleteAll()
    }

    @Test
    fun `deve remover chave pix`() {
        //Cenario
        val chavePix = ChavePix(
            clientId = CLIENT_ID,
            tipoChavePix = TipoChavePix.EMAIL,
            chave = "maxley@mail.com",
            tipoContaItau = TipoContaItau.CONTA_CORRENTE,
            conta = ContaAssociada(
                instituicao = "ITAÚ UNIBANCO S.A.",
                nomeDoTitular = "Rafael M C Ponte",
                cpfDoTitular = "02467781054",
                agencia = "0001",
                numeroDaConta = "291900"
            )
        )
        chavePixRepository.save(chavePix)

        Mockito.`when`(bcbClient.deletaChavePix(
            key = chavePix.chave,
            deletePixKeyRequest = DeletePixKeyRequest(
                key = chavePix.chave,
                participant = chavePix.conta.ITAU_UNIBANCO_ISPB
            )
        )).thenReturn(HttpResponse.ok())

        //Ação
        val response = grpcClient.remove(
            RemoveChavePixRequest.newBuilder()
                .setClienteId(CLIENT_ID.toString())
                .setPixId(chavePix.id.toString())
                .build()
        )

        //Validação
        assertEquals(chavePix.chave, response.chave)
        assertTrue(chavePixRepository.findByChave(chave = chavePix.chave).isEmpty)
    }

    @Test
    fun `nao deve tentar remover chave pix quando nao encontrada`() {
        //Ação
        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.remove(
                RemoveChavePixRequest.newBuilder()
                    .setClienteId(CLIENT_ID.toString())
                    .setPixId(UUID.randomUUID().toString())
                    .build()
            )
        }

        //Validação
        with(exception) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave pix nao encontrada", status.description)
        }

    }

    @Test
    fun `nao deve remover chave pix quando nao for possivel deletar do Banco Central`() {
        //Cenario
        val chavePix = ChavePix(
            clientId = CLIENT_ID,
            tipoChavePix = TipoChavePix.EMAIL,
            chave = "maxley@mail.com",
            tipoContaItau = TipoContaItau.CONTA_CORRENTE,
            conta = ContaAssociada(
                instituicao = "ITAÚ UNIBANCO S.A.",
                nomeDoTitular = "Rafael M C Ponte",
                cpfDoTitular = "02467781054",
                agencia = "0001",
                numeroDaConta = "291900"
            )
        )
        chavePixRepository.save(chavePix)

        Mockito.`when`(bcbClient.deletaChavePix(
            key = chavePix.chave,
            deletePixKeyRequest = DeletePixKeyRequest(
                key = chavePix.chave,
                participant = chavePix.conta.ITAU_UNIBANCO_ISPB
            )
        )).thenReturn(HttpResponse.notFound())

        //Ação
        val exception = assertThrows<StatusRuntimeException>{
            grpcClient.remove(
                RemoveChavePixRequest.newBuilder()
                    .setPixId(chavePix.id.toString())
                    .setClienteId(CLIENT_ID.toString())
                    .build()
            )
        }

        //Validação
        with(exception) {
            assertEquals(
                "Nao foi possivel deletar a chave '${chavePix.chave}' no Banco Central",
                status.description
            )
            assertTrue(chavePixRepository.existsByChave(chavePix.chave))
        }
    }

    @MockBean(BancoCentralClient::class)
    fun bancoCentralClient(): BancoCentralClient? {
        return Mockito.mock(BancoCentralClient::class.java)
    }

    @Factory
    class Clients{
        @Bean
        fun blockingStub(
            @GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel
        ): KeyManagerRemoveGrpcServiceGrpc.KeyManagerRemoveGrpcServiceBlockingStub {
            return KeyManagerRemoveGrpcServiceGrpc.newBlockingStub(channel)
        }
    }
}