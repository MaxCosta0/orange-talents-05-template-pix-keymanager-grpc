package br.com.zupacademy.maxley.integration.bcb.dto

data class ChavePixBcbResponse (
    val keyType: PixKeyTypeBcb,
    val key: String,
    val bankAccount: BankAccountRequest,
    val owner: OwnerRequest,
    val createdAt: String           //TODO("APENAS PARA TESTE - MUDAR PARA FORMATO VALIDO DE TEMPO")
)