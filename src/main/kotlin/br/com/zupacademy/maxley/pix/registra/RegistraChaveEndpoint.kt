package br.com.zupacademy.maxley.pix.registra

import br.com.zupacademy.maxley.KeyManagerServiceGrpc
import br.com.zupacademy.maxley.RegistraChavePixRequest
import br.com.zupacademy.maxley.RegistraChavePixResponse
import br.com.zupacademy.maxley.shared.grpc.ErrorHandler
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton

@ErrorHandler
@Singleton
class RegistraChaveEndpoint(@Inject private val service: NovaChavePixService)
    : KeyManagerServiceGrpc.KeyManagerServiceImplBase() {

    override fun registra(
        request: RegistraChavePixRequest,
        responseObserver: StreamObserver<RegistraChavePixResponse>
    ) {
        val novaChavePixRequest = request.toNovaChavePixRequest()
        val novaChavePix = service.registra(novaChavePixRequest)

        responseObserver.onNext(RegistraChavePixResponse.newBuilder()
            .setClienteId(novaChavePix.clientId.toString())
            .setPixId(novaChavePix.id.toString())
            .build())

        responseObserver.onCompleted()
    }
}