package br.com.zupacademy.maxley.pix.remove

import br.com.zupacademy.maxley.integration.bcb.BancoCentralClient
import br.com.zupacademy.maxley.integration.bcb.dto.DeletePixKeyRequest
import br.com.zupacademy.maxley.pix.ChavePixNaoEncontradaException
import br.com.zupacademy.maxley.integration.itau.ItauContasClient
import br.com.zupacademy.maxley.repository.ChavePixRepository
import br.com.zupacademy.maxley.shared.validation.ValidUUID
import io.micronaut.http.HttpStatus
import io.micronaut.validation.Validated
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.IllegalStateException
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Singleton
@Validated
class RemoveChavePixService(
    @Inject private val chavePixRepository: ChavePixRepository,
    @Inject private val bcbClient: BancoCentralClient
) {

    val logger: Logger = LoggerFactory.getLogger(this::class.java)

    @Transactional
    fun remove(
        @NotNull pixId: Long,
        @NotBlank @ValidUUID clienteId: String
    ) : String {

        val uuidClientId = UUID.fromString(clienteId)

        //A chave pix ja deve ter sido resgistrada previamente
        val chavePix = chavePixRepository.findByIdAndClientId(pixId, uuidClientId)
            .orElseThrow{ChavePixNaoEncontradaException("Chave pix nao encontrada")}

        //Remover chave do Banco Central
        val response = bcbClient.deletaChavePix(
            chavePix.chave,
            DeletePixKeyRequest(
                key = chavePix.chave,
                chavePix.conta.ITAU_UNIBANCO_ISPB
            )
        )

        if (response.status != HttpStatus.OK) {
            throw IllegalStateException("Nao foi possivel deletar a chave '${chavePix.chave}' no Banco Central")
        }

        chavePixRepository.deleteById(pixId)
        return chavePix.chave
    }
}
