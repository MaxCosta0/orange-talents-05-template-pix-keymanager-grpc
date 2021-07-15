package br.com.zupacademy.maxley.pix.lista

import br.com.zupacademy.maxley.*
import br.com.zupacademy.maxley.pix.TipoChavePix
import br.com.zupacademy.maxley.pix.TipoContaItau
import br.com.zupacademy.maxley.repository.ChavePixRepository
import br.com.zupacademy.maxley.shared.grpc.ErrorHandler
import com.google.protobuf.Timestamp
import io.grpc.stub.StreamObserver
import java.time.ZoneId
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@ErrorHandler
@Singleton
class ListaChavePixEndpoint(
    @Inject private val chavePixRepository: ChavePixRepository,
) : KeyManagerListaGrpcSErviceGrpc.KeyManagerListaGrpcSErviceImplBase() {

    override fun lista(
        request: ListaChavePixRequest,
        responseObserver: StreamObserver<ListaChavePixResponse>
    ) {

        if (request.clientId.isNullOrBlank()) {
            throw IllegalArgumentException("clientId nao pode estar em branco ou nulo")
        }

        val clientId = UUID.fromString(request.clientId)

        val chavesDoCliente = chavePixRepository.findAllByClientId(clientId)
            .map { chavePix ->
                ListaChavePixResponse.ChavePixResponse.newBuilder()
                    .setPixId(chavePix.id.toString())
                    .setClientId(chavePix.clientId.toString())
                    .setTipoDeChave(
                        when (chavePix.tipoChavePix) {
                            TipoChavePix.ALEATORIA -> TipoDeChave.ALEATORIA
                            TipoChavePix.EMAIL -> TipoDeChave.EMAIL
                            TipoChavePix.CELULAR -> TipoDeChave.CELULAR
                            TipoChavePix.CPF -> TipoDeChave.CPF
                            else -> TipoDeChave.UNKNOWN_TIPO_CHAVE
                        }
                    )
                    .setChave(chavePix.chave)
                    .setTipoDeConta(
                        when (chavePix.tipoContaItau) {
                            TipoContaItau.CONTA_CORRENTE -> TipoDeConta.CONTA_CORRENTE
                            TipoContaItau.CONTA_POUPANCA -> TipoDeConta.CONTA_POUPANCA
                            else -> TipoDeConta.UNKNOWN_TIPO_CONTA
                        }
                    )
                    .setCriadaEm(chavePix.criadaEm.let {
                        val createAt = it.atZone(ZoneId.of("UTC")).toInstant()
                        Timestamp.newBuilder()
                            .setSeconds(createAt.epochSecond)
                            .setNanos(createAt.nano)
                            .build()
                    })
                    .build()
            }

        responseObserver.onNext(
            ListaChavePixResponse.newBuilder()
                .addAllChavesPix(chavesDoCliente)
                .build()
        )

        responseObserver.onCompleted()

    }
}