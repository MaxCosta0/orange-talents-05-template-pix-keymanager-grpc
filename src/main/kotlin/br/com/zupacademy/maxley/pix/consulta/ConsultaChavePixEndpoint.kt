package br.com.zupacademy.maxley.pix.consulta

import br.com.zupacademy.maxley.ConsultaChavePixRequest
import br.com.zupacademy.maxley.ConsultaChavePixRequest.FiltroCase.*
import br.com.zupacademy.maxley.ConsultaChavePixResponse
import br.com.zupacademy.maxley.KeyManagerConsultaGrpcServiceGrpc
import br.com.zupacademy.maxley.integration.bcb.BancoCentralClient
import br.com.zupacademy.maxley.repository.ChavePixRepository
import br.com.zupacademy.maxley.shared.grpc.ErrorHandler
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.ConstraintViolationException
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

        responseObserver.onNext(chavePixResponse.toConsultaChavePixResponse())
        responseObserver.onCompleted()
    }
}

private fun ConsultaChavePixRequest.toFiltro(validator: Validator): Filtro {

    val filtro = when (filtroCase!!) {
        PIXID -> this.pixId.let {
            Filtro.PorPixId(clientId = it.clientId, pixId = it.pixId)
        }
        CHAVE -> Filtro.PorChave(this.chave)
        FILTRO_NOT_SET -> Filtro.Invalido()
    }

    val violations = validator.validate(filtro)

    if (violations.isNotEmpty()) {
        throw ConstraintViolationException(violations)
    }

    return filtro
}

fun ChavePixResponse.toConsultaChavePixResponse(): ConsultaChavePixResponse {
    return ConsultaChavePixResponse.newBuilder()
        .setClienteId(this.clientId.toString() ?: "")
        .setPixId(this.pixId.toString() ?: "")
        .build()
}