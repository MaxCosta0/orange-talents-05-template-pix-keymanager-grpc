package br.com.zupacademy.maxley.pix

import br.com.zupacademy.maxley.conta.dto.ContaResponse
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client

@Client(value = "http://localhost:9091/api/v1")
interface ClienteItau {
    @Get(value = "/clientes/{clienteId}")
    fun buscaContaPorTipo(clienteId: String, @QueryValue tipoConta: TipoDeConta): HttpResponse<ContaResponse>
}