package br.com.zupacademy.maxley.integration.bcb.dto

data class ChavePixBcbRequest(
    val keyType: PixKeyTypeBcb,
    val key: String,
    val bankAccount: BankAccountRequest,
    val owner: OwnerRequest
)