package br.com.zupacademy.maxley.pix.consulta

import br.com.zupacademy.maxley.integration.bcb.BancoCentralClient
import br.com.zupacademy.maxley.pix.ChavePixNaoEncontradaException
import br.com.zupacademy.maxley.repository.ChavePixRepository
import br.com.zupacademy.maxley.shared.validation.ValidUUID
import io.micronaut.core.annotation.Introspected
import io.micronaut.http.HttpStatus
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*
import javax.validation.constraints.NotBlank

@Introspected
sealed class Filtro {

    abstract fun filtra(
        chavePixRepository: ChavePixRepository,
        bancoCentralClient: BancoCentralClient
    ): ChavePixResponse

    val logger: Logger = LoggerFactory.getLogger(this::class.java)

    @Introspected
    data class PorPixId(
        @field:NotBlank @ValidUUID val clientId: String,
        @field:NotBlank @ValidUUID val pixId: String
    ): Filtro(){

        val uuidPixId: UUID = UUID.fromString(pixId)
        val uuidClienId: UUID = UUID.fromString(clientId)

        override fun filtra(
            chavePixRepository: ChavePixRepository,
            bancoCentralClient: BancoCentralClient
        ): ChavePixResponse {

            logger.info("Filtrando por PixId: $uuidPixId")

            return chavePixRepository.findById(uuidPixId)
                .filter{ it.pertenceAo(clientId = uuidClienId)}
                .map(ChavePixResponse::of)
                .orElseThrow { ChavePixNaoEncontradaException("Chave Pix nao encontrada") }
        }
    }

    @Introspected
    data class PorChave(
        @field:NotBlank val chave: String
    ) : Filtro() {
        override fun filtra(
            chavePixRepository: ChavePixRepository,
            bancoCentralClient: BancoCentralClient
        ): ChavePixResponse {

            logger.info("Filtrando por Chave: $chave")

           return chavePixRepository.findByChave(chave)
               .map(ChavePixResponse::of)
               .orElseGet {

                   logger.info("Consultando chave '$chave' no Banco Central")

                   val consultaChaveBcbResponse = bancoCentralClient.consultaChave(chave)

                   when (consultaChaveBcbResponse.status) {
                       HttpStatus.OK -> consultaChaveBcbResponse.body()?.toChavePixResponse()
                       else -> throw ChavePixNaoEncontradaException("Chave Pix nao encontrada")
                   }
               }
        }

    }

    @Introspected
    class Invalido : Filtro() {
        override fun filtra(
            chavePixRepository: ChavePixRepository,
            bancoCentralClient: BancoCentralClient
        ): ChavePixResponse {
            throw IllegalArgumentException("Chave Pix invalida ou nao informada")
        }

    }
}
