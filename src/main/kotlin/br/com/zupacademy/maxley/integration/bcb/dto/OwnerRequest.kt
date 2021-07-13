package br.com.zupacademy.maxley.integration.bcb.dto

data class OwnerRequest(
    val type: TypePerson,
    val name: String,
    val taxIdNumber: String
)