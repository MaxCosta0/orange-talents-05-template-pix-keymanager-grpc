package br.com.zupacademy.maxley.model

import br.com.zupacademy.maxley.pix.TipoChavePix
import br.com.zupacademy.maxley.pix.TipoContaItau
import java.time.LocalDate
import java.time.LocalDateTime
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
    var id: UUID? = null

    val criadaEm: LocalDateTime = LocalDateTime.now()

    fun atualizaChave(chave: String) {
        if (tipoChavePix == TipoChavePix.ALEATORIA) {
            this.chave = chave
        }
    }

    fun pertenceAo(clientId: UUID): Boolean {
        return this.clientId == clientId
    }
}
