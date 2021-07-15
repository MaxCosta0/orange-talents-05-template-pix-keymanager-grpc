package br.com.zupacademy.maxley.pix.consulta

import br.com.zupacademy.maxley.ConsultaChavePixResponse
import br.com.zupacademy.maxley.TipoDeChave
import br.com.zupacademy.maxley.TipoDeConta
import br.com.zupacademy.maxley.pix.TipoChavePix
import br.com.zupacademy.maxley.pix.TipoContaItau
import com.google.protobuf.Timestamp
import java.time.ZoneId

class ConversorParaConsultaChavePixResponse {

    companion object{
        fun converter(chavePixResponse: ChavePixResponse): ConsultaChavePixResponse? {
            return ConsultaChavePixResponse.newBuilder()
                .setClienteId(chavePixResponse.clientId?.toString() ?: "")
                .setPixId(chavePixResponse.pixId?.toString() ?: "")
                .setChave(
                    ConsultaChavePixResponse.ChavePix.newBuilder()
                        .setTipo(
                            when (chavePixResponse.tipoDaChave) {
                                TipoChavePix.ALEATORIA -> TipoDeChave.ALEATORIA
                                TipoChavePix.EMAIL -> TipoDeChave.EMAIL
                                TipoChavePix.CELULAR -> TipoDeChave.CELULAR
                                TipoChavePix.CPF -> TipoDeChave.CPF
                                else -> TipoDeChave.UNKNOWN_TIPO_CHAVE
                            }
                        )
                        .setChave(chavePixResponse.chave)
                        .setConta(
                            ConsultaChavePixResponse.ChavePix.ContaInfo.newBuilder()
                                .setTipo(
                                    when (chavePixResponse.tipoDaConta) {
                                        TipoContaItau.CONTA_CORRENTE -> TipoDeConta.CONTA_CORRENTE
                                        TipoContaItau.CONTA_POUPANCA -> TipoDeConta.CONTA_POUPANCA
                                        else -> TipoDeConta.UNKNOWN_TIPO_CONTA
                                    }
                                )
                                .setInstituicao(chavePixResponse.instituicao)
                                .setNomeDoTitular(chavePixResponse.nomeTitular)
                                .setCpfDoTitular(chavePixResponse.cpfTitular)
                                .setAgencia(chavePixResponse.agencia)
                                .setNumeroDaConta(chavePixResponse.numeroDaConta)
                                .build()
                        )
                        .setCriadaEm(chavePixResponse.cadastradaEm.let {
                            val createAt = it.atZone(ZoneId.of("UTC")).toInstant()

                            Timestamp.newBuilder()
                                .setSeconds(createAt.epochSecond)
                                .setNanos(createAt.nano)
                        })
                        .build()
                )
                .build()
        }
    }
}