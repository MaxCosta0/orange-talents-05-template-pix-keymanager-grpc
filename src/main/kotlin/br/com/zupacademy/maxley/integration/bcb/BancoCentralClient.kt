package br.com.zupacademy.maxley.integration.bcb

import br.com.zupacademy.maxley.integration.bcb.dto.ChavePixBcbRequest
import br.com.zupacademy.maxley.integration.bcb.dto.ChavePixBcbResponse
import br.com.zupacademy.maxley.integration.bcb.dto.DeletePixKeyRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.http.client.annotation.Client

@Client("http://localhost:8082/api/v1/pix/keys")
interface BancoCentralClient {

    @Post(consumes = [MediaType.APPLICATION_XML], produces = [MediaType.APPLICATION_XML])
    fun registraChavePix(@Body request : ChavePixBcbRequest): HttpResponse<ChavePixBcbResponse>

    @Delete("/{key}")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    fun deletaChavePix(
        @PathVariable key: String,
        @Body deletePixKeyRequest: DeletePixKeyRequest
    ): HttpResponse<Map<String, Any>>
}