package br.com.zupacademy.maxley.pix.registra

import br.com.zupacademy.maxley.RegistraChavePixRequest
import br.com.zupacademy.maxley.pix.TipoChavePix
import br.com.zupacademy.maxley.pix.TipoContaItau

fun RegistraChavePixRequest.toNovaChavePixRequest(): NovaChavePixRequest {
    return NovaChavePixRequest(
        clientId = this.clientId,
        tipoChavePix = TipoChavePix.valueOf(this.tipoDeChave.name),
        chave = this.chave,
        tipoContaItau = TipoContaItau.valueOf(this.tipoDeConta.name)
    )
}