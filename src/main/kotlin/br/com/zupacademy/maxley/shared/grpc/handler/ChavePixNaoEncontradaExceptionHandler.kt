package br.com.zupacademy.maxley.shared.grpc.handler

import br.com.zupacademy.maxley.pix.ChavePixNaoEncontradaException
import br.com.zupacademy.maxley.shared.grpc.ExceptionHandler
import io.grpc.Status
import javax.inject.Singleton

@Singleton
class ChavePixNaoEncontradaExceptionHandler: ExceptionHandler<ChavePixNaoEncontradaException> {
    override fun handle(e: ChavePixNaoEncontradaException): ExceptionHandler.StatusWithDetails {
        return ExceptionHandler.StatusWithDetails(
            Status.NOT_FOUND
                .withDescription(e.message)
                .withCause(e)
        )
    }

    override fun supports(e: Exception): Boolean {
        return e is ChavePixNaoEncontradaException
    }
}