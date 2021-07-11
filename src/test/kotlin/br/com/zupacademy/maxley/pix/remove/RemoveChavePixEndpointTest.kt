package br.com.zupacademy.maxley.pix.remove

import br.com.zupacademy.maxley.KeyManagerRemoveGrpcServiceGrpc
import br.com.zupacademy.maxley.RemoveChavePixRequest
import br.com.zupacademy.maxley.pix.TipoChavePix
import br.com.zupacademy.maxley.pix.TipoContaItau
import br.com.zupacademy.maxley.pix.model.ChavePix
import br.com.zupacademy.maxley.pix.repository.ChavePixRepository
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

@MicronautTest(transactional = false)
internal class RemoveChavePixEndpointTest(
    private val chavePixRepository: ChavePixRepository,
    private val grpcClient: KeyManagerRemoveGrpcServiceGrpc.KeyManagerRemoveGrpcServiceBlockingStub
) {

    companion object{
        private val CLIENT_ID: UUID = UUID.randomUUID()
    }

    @BeforeEach
    fun setup() {
        chavePixRepository.deleteAll()
    }

    @Test
    fun `deve remover chave pix`() {
        //Cenario
        val chavePix = ChavePix(
            clientId = CLIENT_ID,
            tipoChavePix = TipoChavePix.EMAIL,
            chave = "maxley@mail.com",
            tipoContaItau = TipoContaItau.CONTA_CORRENTE
        )

        chavePixRepository.save(chavePix)

        //Ação
        val response = grpcClient.remove(
            RemoveChavePixRequest.newBuilder()
                .setClienteId(CLIENT_ID.toString())
                .setPixId(1L)
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
                    .setPixId(1L)
                    .build()
            )
        }

        //Validação
        with(exception) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave pix nao encontrada", status.description)
        }

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