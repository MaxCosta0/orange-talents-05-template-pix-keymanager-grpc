package br.com.zupacademy.maxley.pix.registra

import br.com.zupacademy.maxley.KeyManagerServiceGrpc
import br.com.zupacademy.maxley.RegistraChavePixRequest
import br.com.zupacademy.maxley.RegistraChavePixResponse
import br.com.zupacademy.maxley.shared.ErrorAroundHandler
import io.grpc.stub.StreamObserver
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@ErrorAroundHandler
@Singleton
class RegistraChaveEndpoint(@Inject val service: NovaChavePixService,)
    : KeyManagerServiceGrpc.KeyManagerServiceImplBase() {

    override fun registra(
        request: RegistraChavePixRequest,
        responseObserver: StreamObserver<RegistraChavePixResponse>
    ) {
        val novaChavePixRequest = request.toNovaChavePixRequest()
        val novaChavePix = service.registra(novaChavePixRequest)

        responseObserver.onNext(RegistraChavePixResponse.newBuilder()
            .setClienteId(request.clientId)
            .setPixId(UUID.randomUUID().toString())
            .build())

        responseObserver.onCompleted()
    }
}