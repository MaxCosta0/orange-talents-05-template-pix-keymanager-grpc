package br.com.zupacademy.maxley.pix.registra

import br.com.zupacademy.maxley.integration.bcb.BancoCentralClient
import br.com.zupacademy.maxley.integration.bcb.dto.ChavePixBcbRequest
import br.com.zupacademy.maxley.integration.itau.ItauContasClient
import br.com.zupacademy.maxley.model.ChavePix
import br.com.zupacademy.maxley.pix.ChavePixExistenteException
import br.com.zupacademy.maxley.repository.ChavePixRepository
import io.micronaut.http.HttpStatus
import io.micronaut.validation.Validated
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@Validated
@Singleton
class NovaChavePixService(
    @Inject val chavePixRepository: ChavePixRepository,
    @Inject val itauContasClient: ItauContasClient,
    @Inject val bcbClient: BancoCentralClient
) {

    @Transactional
    fun registra(@Valid novaChavePixRequest: NovaChavePixRequest): ChavePix {

        //A chave deve ser unica no banco de dados
        chavePixRepository.findByChave(novaChavePixRequest.chave).ifPresent {
            throw ChavePixExistenteException("Chave pix '${novaChavePixRequest.chave}' ja existe")
        }

        //Deve haver uma conta no sistema ERP Itau associada ao clientId recebido
        val contaResponse = itauContasClient
            .buscaContaPorTipo(
                novaChavePixRequest.clientId,
                novaChavePixRequest.tipoContaItau
            )

        val conta = contaResponse.body()?.toModel() ?:
        throw IllegalStateException("ClienteId '${novaChavePixRequest.clientId}' nao encontrado")

        val chavePix =  chavePixRepository.save(novaChavePixRequest.toChavePix(conta))

        val chavePixBcbRequest = ChavePixBcbRequest(chavePix)

        val bcbResponse =  bcbClient.registraChavePix(chavePixBcbRequest)

        if (bcbResponse.status != HttpStatus.CREATED) {
            throw IllegalStateException(
                "Nao foi possivel registrar chave pix '${chavePix.chave}' no Banco Central"
            )
        }

        chavePix.atualizaChave(bcbResponse.body()!!.key)

        return chavePix
    }

}

