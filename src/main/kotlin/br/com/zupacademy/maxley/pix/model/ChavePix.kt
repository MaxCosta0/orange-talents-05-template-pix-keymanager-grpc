package br.com.zupacademy.maxley.pix.model

import br.com.zupacademy.maxley.pix.TipoChavePix
import br.com.zupacademy.maxley.pix.TipoContaItau
import java.util.*
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
class ChavePix(
    val clientId: UUID,
    val tipoChavePix: TipoChavePix,
    val chave: String,
    val tipoContaItau: TipoContaItau
) {
    @Id @GeneratedValue
    var id: Long? = null
}
