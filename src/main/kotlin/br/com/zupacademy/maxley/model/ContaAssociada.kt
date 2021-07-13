package br.com.zupacademy.maxley.model

import javax.persistence.Embeddable

@Embeddable
class ContaAssociada (
    val instituicao: String,
    val nomeDoTitular: String,
    val cpfDoTitular: String,
    val agencia: String,
    val numeroDaConta: String
){
    val ITAU_UNIBANCO_ISPB: String = "60701190"
}
