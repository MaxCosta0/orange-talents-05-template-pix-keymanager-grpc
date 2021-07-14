package br.com.zupacademy.maxley.integration.bcb.dto

import br.com.zupacademy.maxley.pix.TipoContaItau
import br.com.zupacademy.maxley.pix.consulta.ChavePixResponse
import java.time.LocalDateTime

class ConsultaChaveBcbResponse (
    val keyType: PixKeyTypeBcb,
    val key: String,
    val bankAccount: BankAccountRequest,
    val owner: OwnerRequest,
    val createdAt: LocalDateTime
) {

    fun toChavePixResponse(): ChavePixResponse {
        return ChavePixResponse(
            tipoDaChave = this.keyType.domainType!!,
            chave = this.key,
            nomeTitular = owner.name,
            cpfTitular = owner.taxIdNumber,
            instituicao = "ITAU Unibanco",          //TODO("Provisorio até encontrar uma forma de deixar dinamico")
            agencia = bankAccount.branch,
            numeroDaConta = bankAccount.accountNumber,
            tipoDaConta = when (bankAccount.accountType) {
                AccountType.CACC -> TipoContaItau.CONTA_CORRENTE
                AccountType.SVGS -> TipoContaItau.CONTA_POUPANCA
                else -> TipoContaItau.UNKNOWN_TIPO_CONTA
            },
            cadastradaEm = this.createdAt
        )
    }
}