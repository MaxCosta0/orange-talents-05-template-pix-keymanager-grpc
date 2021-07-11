package br.com.zupacademy.maxley.shared.grpc

import io.grpc.BindableService
import io.grpc.stub.StreamObserver
import io.micronaut.aop.MethodInterceptor
import io.micronaut.aop.MethodInvocationContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.Exception
import javax.inject.Singleton

@Singleton
class ExceptionHandlerInterceptor(private val resolver: ExceptionHandlerResolver) : MethodInterceptor<BindableService, Any?> {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    override fun intercept(context: MethodInvocationContext<BindableService, Any?>): Any? {
        try {
            context.proceed()
        } catch (e: Exception) {

            logger.error("Handling the exception '${e.javaClass.name}' while processing the call: ${context.targetMethod}", e)

            @Suppress("UNCHECKED_CAST")
            val handler = resolver.resolve(e) as ExceptionHandler<Exception>
            val status = handler.handle(e)

//            val responseObserver = context.parameterValues[1] as StreamObserver<*>
//
//            responseObserver.onError(status.asRuntimeException())

            GrpcEndpointArguments(context).response()
                .onError(status.asRuntimeException())
        }
        return null
    }
}

class GrpcEndpointArguments(val context: MethodInvocationContext<BindableService, Any?>) {
    fun response(): StreamObserver<*> {
        return context.parameterValues[1] as StreamObserver<*>
    }
}
