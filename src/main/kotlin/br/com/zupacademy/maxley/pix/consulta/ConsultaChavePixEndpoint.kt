package br.com.zupacademy.maxley.pix.consulta

import br.com.zupacademy.maxley.ConsultaChavePixRequest
import br.com.zupacademy.maxley.ConsultaChavePixResponse
import br.com.zupacademy.maxley.KeyManagerConsultaGrpcServiceGrpc
import br.com.zupacademy.maxley.integration.bcb.BancoCentralClient
import br.com.zupacademy.maxley.repository.ChavePixRepository
import br.com.zupacademy.maxley.shared.grpc.ErrorHandler
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Validator

@ErrorHandler
@Singleton
class ConsultaChavePixEndpoint(
    @Inject private val chavePixRepository: ChavePixRepository,
    @Inject private val bcbClient: BancoCentralClient,
    @Inject private val validator: Validator
): KeyManagerConsultaGrpcServiceGrpc.KeyManagerConsultaGrpcServiceImplBase() {
    override fun consulta(
        request: ConsultaChavePixRequest,
        responseObserver: StreamObserver<ConsultaChavePixResponse>
    ) {

        val filtroDaRequisicao = request.toFiltro(validator)
        val chavePixResponse = filtroDaRequisicao.filtra(chavePixRepository, bcbClient)

        responseObserver.onNext(ConversorParaConsultaChavePixResponse.converter(chavePixResponse))
        responseObserver.onCompleted()
    }
}

