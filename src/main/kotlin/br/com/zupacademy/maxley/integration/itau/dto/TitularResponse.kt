package br.com.zupacademy.maxley.conta.dto

import java.util.*

data class TitularResponse(
    val id: UUID,
    val nome: String,
    val cpf: String
)