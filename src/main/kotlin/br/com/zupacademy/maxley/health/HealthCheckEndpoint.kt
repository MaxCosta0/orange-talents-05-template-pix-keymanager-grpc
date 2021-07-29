package br.com.zupacademy.maxley.health

import br.com.zupacademy.maxley.HealthCheckRequest
import br.com.zupacademy.maxley.HealthCheckResponse
import br.com.zupacademy.maxley.HealthGrpc
import io.grpc.stub.StreamObserver
import javax.inject.Singleton

@Singleton
class HealthCheckEndpoint : HealthGrpc.HealthImplBase() {

    override fun check(request: HealthCheckRequest?, responseObserver: StreamObserver<HealthCheckResponse>?) {
        responseObserver?.onNext(
            HealthCheckResponse.newBuilder()
                .setStatus(HealthCheckResponse.ServingStatus.SERVING)
                .build()
        )

        responseObserver?.onCompleted()
    }

    override fun watch(request: HealthCheckRequest?, responseObserver: StreamObserver<HealthCheckResponse>?) {
        responseObserver?.onNext(
            HealthCheckResponse.newBuilder()
                .setStatus(HealthCheckResponse.ServingStatus.SERVING)
                .build()
        )
    }
}