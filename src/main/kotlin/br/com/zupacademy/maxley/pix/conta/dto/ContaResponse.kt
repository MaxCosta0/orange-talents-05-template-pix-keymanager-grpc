package br.com.zupacademy.maxley.conta.dto

data class ContaResponse(
    val id: String,
    val nome: String,
    val cpf: String,
    val instituicao: Instituicao
)
