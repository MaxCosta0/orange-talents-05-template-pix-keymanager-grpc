package br.com.zupacademy.maxley.model

import br.com.zupacademy.maxley.pix.TipoChavePix
import br.com.zupacademy.maxley.pix.TipoContaItau
import java.util.*
import javax.persistence.*

@Entity
class ChavePix(
    val clientId: UUID,
    @Enumerated(EnumType.STRING)
    val tipoChavePix: TipoChavePix,
    var chave: String,
    @Enumerated(EnumType.STRING)
    val tipoContaItau: TipoContaItau,
    @Embedded
    val conta: ContaAssociada
) {
    @Id @GeneratedValue
    var id: Long? = null

    fun atualizaChave(chave: String) {
        if (tipoChavePix == TipoChavePix.ALEATORIA) {
            this.chave = chave
        }
    }
}
