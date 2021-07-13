package br.com.zupacademy.maxley.integration.bcb.dto

data class BankAccountRequest(
    val participant: String,
    val branch: String,
    val accountNumber: String,
    val accountType: AccountType
)
