package br.com.zupacademy.maxley.pix.registra

import br.com.zupacademy.maxley.RegistraChavePixRequest
import br.com.zupacademy.maxley.pix.TipoDeChave
import br.com.zupacademy.maxley.pix.TipoDeConta

fun RegistraChavePixRequest.toNovaChavePixRequest(): NovaChavePixRequest {
    return NovaChavePixRequest(
        clientId = this.clientId,
        tipoDeChave = TipoDeChave.valueOf(this.tipoDeChave.name),
        chave = this.chave,
        tipodeConta = TipoDeConta.valueOf(this.tipoDeConta.name)
    )
}