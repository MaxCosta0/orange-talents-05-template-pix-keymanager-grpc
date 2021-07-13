package br.com.zupacademy.maxley.conta.dto

import br.com.zupacademy.maxley.model.ContaAssociada

data class ContaResponse(
    val tipo: String,
    val agencia: String,
    val numero: String,
    val instituicao: InstituicaoResponse,
    val titular: TitularResponse
) {
    fun toModel(): ContaAssociada {
        return ContaAssociada(
            instituicao = this.instituicao.nome,
            nomeDoTitular = this.titular.nome,
            cpfDoTitular = this.titular.cpf,
            agencia = this.agencia,
            numeroDaConta = this.numero
        )
    }
}
