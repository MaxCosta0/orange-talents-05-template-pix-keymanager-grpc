package br.com.zupacademy.maxley.pix.remove

import br.com.zupacademy.maxley.KeyManagerRemoveGrpcServiceGrpc
import br.com.zupacademy.maxley.RemoveChavePixRequest
import br.com.zupacademy.maxley.RemoveChavePixResponse
import br.com.zupacademy.maxley.shared.grpc.ErrorHandler
import io.grpc.stub.StreamObserver
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@ErrorHandler
class RemoveChavePixEndpoint(
    @Inject private val service: RemoveChavePixService
) : KeyManagerRemoveGrpcServiceGrpc.KeyManagerRemoveGrpcServiceImplBase() {

    override fun remove(
        request: RemoveChavePixRequest,
        responseObserver: StreamObserver<RemoveChavePixResponse>
    ) {

        val chave = service.remove(pixId = request.pixId, clienteId = request.clienteId)

        responseObserver.onNext(RemoveChavePixResponse.newBuilder().setChave(chave).build())

        responseObserver.onCompleted()
    }
}