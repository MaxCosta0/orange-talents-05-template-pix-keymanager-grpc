package br.com.zupacademy.maxley.shared

import io.grpc.Status
import io.grpc.stub.StreamObserver
import io.micronaut.aop.InterceptorBean
import io.micronaut.aop.MethodInterceptor
import io.micronaut.aop.MethodInvocationContext
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException
import java.lang.RuntimeException
import javax.inject.Singleton

@Singleton
@InterceptorBean(ErrorAroundHandler::class)
class ErrorAroundHandlerInterceptor: MethodInterceptor<Any, Any> {

    override fun intercept(context: MethodInvocationContext<Any, Any>): Any? {
        try {
            return context.proceed()
        } catch (ex: Exception) {

            val responseObserver = context.parameterValues[1] as StreamObserver<*>

            val status = when(ex){
                is IllegalArgumentException -> Status.ALREADY_EXISTS
                    .withCause(ex)
                    .withDescription(ex.message)

                is IllegalStateException -> Status.NOT_FOUND
                    .withCause(ex)
                    .withDescription(ex.message)

                else -> Status.INVALID_ARGUMENT
                    .withCause(ex)
                    .withDescription(ex.message)
            }

            responseObserver.onError(status.asRuntimeException())
        }

        return null
    }
}