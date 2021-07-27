package br.com.zupacademy.maxley.integration.bcb.dto

import br.com.zupacademy.maxley.model.ChavePix
import br.com.zupacademy.maxley.pix.TipoContaItau

data class ChavePixBcbRequest(
    val keyType: PixKeyTypeBcb?,
    val key: String?,
    val bankAccount: BankAccountRequest?,
    val owner: OwnerRequest?
){
    constructor(chavePix: ChavePix) : this(
        keyType = PixKeyTypeBcb.by(chavePix.tipoChavePix),
        key = chavePix.chave,
        bankAccount = BankAccountRequest(
            participant = chavePix.conta.ITAU_UNIBANCO_ISPB,
            branch = chavePix.conta.agencia,
            accountNumber = chavePix.conta.numeroDaConta,
            accountType = when (chavePix.tipoContaItau) {
                TipoContaItau.CONTA_CORRENTE -> AccountType.CACC
                TipoContaItau.CONTA_POUPANCA -> AccountType.SVGS
                else -> AccountType.UNKNOWN
            }
        ),
        owner = OwnerRequest(
            type = TypePerson.NATURAL_PERSON,
            name = chavePix.conta.nomeDoTitular,
            taxIdNumber = chavePix.conta.cpfDoTitular
        )
    )

//    fun instanciaDe(chavePix: ChavePix): ChavePixBcbRequest {
//        return ChavePixBcbRequest(
//            keyType = PixKeyTypeBcb.by(chavePix.tipoChavePix),
//            key = chavePix.chave,
//            bankAccount = BankAccountRequest(
//                participant = chavePix.conta.ITAU_UNIBANCO_ISPB,
//                branch = chavePix.conta.agencia,
//                accountNumber = chavePix.conta.numeroDaConta,
//                accountType = when (chavePix.tipoContaItau) {
//                    TipoContaItau.CONTA_CORRENTE -> AccountType.CACC
//                    TipoContaItau.CONTA_POUPANCA -> AccountType.SVGS
//                    else -> AccountType.UNKNOWN
//                }
//            ),
//            owner = OwnerRequest(
//                type = TypePerson.NATURAL_PERSON,
//                name = chavePix.conta.nomeDoTitular,
//                taxIdNumber = chavePix.conta.cpfDoTitular
//            )
//        )
//    }
}