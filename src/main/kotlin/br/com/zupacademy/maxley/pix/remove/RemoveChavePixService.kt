package br.com.zupacademy.maxley.pix.remove

import br.com.zupacademy.maxley.pix.ChavePixNaoEncontradaException
import br.com.zupacademy.maxley.pix.ItauContasClient
import br.com.zupacademy.maxley.pix.repository.ChavePixRepository
import br.com.zupacademy.maxley.shared.validation.ValidUUID
import io.micronaut.validation.Validated
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

@Singleton
@Validated
class RemoveChavePixService(
    @Inject private val chavePixRepository: ChavePixRepository,
    @Inject private val itauContasClient: ItauContasClient
) {

    val logger: Logger = LoggerFactory.getLogger(this::class.java)

    fun remove(
        @NotNull pixId: Long,
        @NotBlank @ValidUUID clienteId: String
    ) : String {

        val uuidClientId = UUID.fromString(clienteId)

        //A chave pix ja deve ter sido resgistrada previamente
       val chavePix = chavePixRepository.findByIdAndClientId(pixId, uuidClientId)
           .orElseThrow{ChavePixNaoEncontradaException("Chave pix nao encontrada")}

        chavePixRepository.deleteById(pixId)
        return chavePix.chave
    }
}
