package br.com.zupacademy.maxley.pix.model

import br.com.zupacademy.maxley.pix.TipoDeChave
import br.com.zupacademy.maxley.pix.TipoDeConta
import java.util.*
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
class ChavePix(
    val clientId: UUID,
    val tipoDeChave: TipoDeChave,
    val chave: String,
    val tipodeConta: TipoDeConta
) {
    @Id @GeneratedValue
    var id: Long? = null
}
