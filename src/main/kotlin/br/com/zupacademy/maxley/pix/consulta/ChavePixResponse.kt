package br.com.zupacademy.maxley.pix.consulta

import br.com.zupacademy.maxley.ConsultaChavePixResponse
import br.com.zupacademy.maxley.model.ChavePix
import br.com.zupacademy.maxley.pix.TipoChavePix
import br.com.zupacademy.maxley.pix.TipoContaItau
import java.time.LocalDateTime
import java.util.*

data class ChavePixResponse (
    val pixId: UUID? = null,
    val clientId: UUID? = null,
    val tipoDaChave: TipoChavePix,
    val chave: String,
    val nomeTitular: String,
    val cpfTitular: String,
    val instituicao: String,
    val agencia: String,
    val numeroDaConta: String,
    val tipoDaConta: TipoContaItau,
    val cadastradaEm: LocalDateTime
){

   companion object {
        fun of(chavePix: ChavePix): ChavePixResponse {
            return ChavePixResponse(
                pixId = chavePix.id,
                clientId = chavePix.clientId,
                tipoDaChave = chavePix.tipoChavePix,
                chave = chavePix.chave,
                nomeTitular = chavePix.conta.nomeDoTitular,
                cpfTitular = chavePix.conta.cpfDoTitular,
                instituicao = chavePix.conta.instituicao,
                agencia = chavePix.conta.agencia,
                numeroDaConta = chavePix.conta.numeroDaConta,
                tipoDaConta = chavePix.tipoContaItau,
                cadastradaEm = chavePix.criadaEm
            )
        }
    }

}