package br.com.zupacademy.maxley.pix.registra

import br.com.zupacademy.maxley.pix.ItauContasClient
import br.com.zupacademy.maxley.pix.model.ChavePix
import br.com.zupacademy.maxley.pix.repository.ChavePixRepository
import br.com.zupacademy.maxley.pix.ChavePixExistenteException
import io.micronaut.validation.Validated
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Valid

@Validated
@Singleton
class NovaChavePixService(@Inject val chavePixRepository: ChavePixRepository,
                          @Inject val itauContasClient: ItauContasClient,) {

    val log: Logger = LoggerFactory.getLogger(this::class.java)

    fun registra(@Valid novaChavePixRequest: NovaChavePixRequest): ChavePix {

        //A chave deve ser unica no banco de dados
        val possivelChavePix = chavePixRepository.findByChave(novaChavePixRequest.chave)
        if(!possivelChavePix.isEmpty)
            throw ChavePixExistenteException("Chave pix '${novaChavePixRequest.chave}' ja existe")

        //Deve haver uma conta no sistema ERP Itau associada ao cliente Id recebido
        val contaResponse = itauContasClient.buscaContaPorTipo(novaChavePixRequest.clientId,
                                                        novaChavePixRequest.tipoContaItau)

        if (contaResponse.body() == null)
            throw IllegalStateException("ClienteId '${novaChavePixRequest.clientId}' nao encontrado")

        return chavePixRepository.save(novaChavePixRequest.toChavePix())
    }

}
