package br.com.zupacademy.maxley.integration.itau

import br.com.zupacademy.maxley.conta.dto.ContaResponse
import br.com.zupacademy.maxley.pix.TipoContaItau
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client

@Client(value = "http://localhost:9091/api/v1")
interface ItauContasClient {
    @Get(value = "/clientes/{clienteId}/contas{?tipo}")
    fun buscaContaPorTipo(@PathVariable clienteId: String, @QueryValue tipo: TipoContaItau): HttpResponse<ContaResponse>
}