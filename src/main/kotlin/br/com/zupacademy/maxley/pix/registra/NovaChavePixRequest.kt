package br.com.zupacademy.maxley.pix.registra

import br.com.zupacademy.maxley.pix.TipoDeChave
import br.com.zupacademy.maxley.pix.TipoDeConta
import br.com.zupacademy.maxley.pix.model.ChavePix
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
    val tipoDeChave: TipoDeChave,
    @field:Size(max = 77)
    val chave: String?,
    @field:NotNull
    val tipodeConta: TipoDeConta
){
    fun toChavePix(): ChavePix{
        return ChavePix(
            clientId = UUID.fromString(this.clientId),
            tipoDeChave = this.tipoDeChave,
            chave = if(this.tipoDeChave == TipoDeChave.ALEATORIA) UUID.randomUUID().toString() else this.chave!!,
            tipodeConta = this.tipodeConta
        )
    }
}