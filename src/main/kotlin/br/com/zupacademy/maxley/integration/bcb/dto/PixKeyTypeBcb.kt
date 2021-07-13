package br.com.zupacademy.maxley.integration.bcb.dto

import br.com.zupacademy.maxley.pix.TipoChavePix
import java.lang.IllegalStateException

enum class PixKeyTypeBcb(val domainType: TipoChavePix?) {
    CPF(TipoChavePix.CPF),
    CNPJ(null),
    PHONE(TipoChavePix.CELULAR),
    EMAIL(TipoChavePix.EMAIL),
    RANDOM(TipoChavePix.ALEATORIA);

    companion object{

        private val mapping = PixKeyTypeBcb.values().associateBy(PixKeyTypeBcb::domainType)

        fun by(domainType: TipoChavePix): PixKeyTypeBcb {
            return mapping[domainType] ?: throw IllegalStateException("Pix key invalid or not found $domainType")
        }
    }
}