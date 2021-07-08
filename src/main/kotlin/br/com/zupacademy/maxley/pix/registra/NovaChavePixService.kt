package br.com.zupacademy.maxley.pix.registra

import br.com.zupacademy.maxley.pix.ClienteItau
import br.com.zupacademy.maxley.pix.model.ChavePix
import br.com.zupacademy.maxley.pix.repository.ChavePixRepository
import io.micronaut.validation.Validated
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Valid

@Validated
@Singleton
class NovaChavePixService(@Inject val chavePixRepository: ChavePixRepository,
                          @Inject val clienteItau: ClienteItau,) {

    val log: Logger = LoggerFactory.getLogger(this::class.java)

    fun registra(@Valid novaChavePixRequest: NovaChavePixRequest): ChavePix {

        //A chave deve ser unica no banco de dados
        val possivelChavePix = chavePixRepository.findByChave(novaChavePixRequest.chave)
        if(possivelChavePix.isPresent)
            throw IllegalArgumentException("A chave '${novaChavePixRequest.chave}' ja existe")

        //Deve haver uma conta no sistema ERP Itau associada ao cliente Id recebido
        val contaResponse = clienteItau.buscaContaPorTipo(novaChavePixRequest.clientId,
                                                        novaChavePixRequest.tipodeConta)

        if (contaResponse.body() == null)
            throw IllegalStateException("ClienteId '${novaChavePixRequest.clientId}' nao encontrado")

        return chavePixRepository.save(novaChavePixRequest.toChavePix())
    }

}
