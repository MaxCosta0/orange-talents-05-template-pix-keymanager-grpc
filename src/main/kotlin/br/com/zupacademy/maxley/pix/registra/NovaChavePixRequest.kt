package br.com.zupacademy.maxley.pix.registra

import br.com.zupacademy.maxley.pix.TipoChavePix
import br.com.zupacademy.maxley.pix.TipoContaItau
import br.com.zupacademy.maxley.model.ChavePix
import br.com.zupacademy.maxley.model.ContaAssociada
import br.com.zupacademy.maxley.shared.ValidPixKey
import br.com.zupacademy.maxley.shared.validation.ValidUUID
import io.micronaut.core.annotation.Introspected
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@ValidPixKey
@Introspected
data class NovaChavePixRequest(
    @field:NotBlank
    @ValidUUID
    val clientId: String,
    @field:NotNull
    val tipoChavePix: TipoChavePix,
    @field:Size(max = 77)
    val chave: String?,
    @field:NotNull
    val tipoContaItau: TipoContaItau
){
    fun toChavePix(conta: ContaAssociada): ChavePix {
        return ChavePix(
            clientId = UUID.fromString(this.clientId),
            tipoChavePix = this.tipoChavePix,
            chave = if(this.tipoChavePix == TipoChavePix.ALEATORIA) UUID.randomUUID().toString() else this.chave!!,
            tipoContaItau = this.tipoContaItau,
            conta = conta
        )
    }
}