package br.com.zupacademy.maxley.integration.bcb.dto

import java.time.LocalDateTime

data class ChavePixBcbResponse (
    val keyType: PixKeyTypeBcb,
    val key: String,
    val bankAccount: BankAccountRequest,
    val owner: OwnerRequest,
    val createdAt: LocalDateTime           //TODO("APENAS PARA TESTE - MUDAR PARA FORMATO VALIDO DE TEMPO")
)