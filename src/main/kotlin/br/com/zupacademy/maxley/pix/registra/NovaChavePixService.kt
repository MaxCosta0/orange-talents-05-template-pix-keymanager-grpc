package br.com.zupacademy.maxley.pix.registra

import br.com.zupacademy.maxley.integration.bcb.BancoCentralClient
import br.com.zupacademy.maxley.integration.bcb.dto.*
import br.com.zupacademy.maxley.integration.itau.ItauContasClient
import br.com.zupacademy.maxley.model.ChavePix
import br.com.zupacademy.maxley.repository.ChavePixRepository
import br.com.zupacademy.maxley.pix.ChavePixExistenteException
import br.com.zupacademy.maxley.pix.TipoChavePix
import br.com.zupacademy.maxley.pix.TipoContaItau
import io.micronaut.http.HttpStatus
import io.micronaut.validation.Validated
import org.slf4j.Logger
import org.slf4j.LoggerFactory
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

    val logger: Logger = LoggerFactory.getLogger(this::class.java)

    @Transactional
    fun registra(@Valid novaChavePixRequest: NovaChavePixRequest): ChavePix {

        //A chave deve ser unica no banco de dados
        val possivelChavePix = chavePixRepository.findByChave(novaChavePixRequest.chave)
        if(!possivelChavePix.isEmpty)
            throw ChavePixExistenteException("Chave pix '${novaChavePixRequest.chave}' ja existe")

        //Deve haver uma conta no sistema ERP Itau associada ao cliente Id recebido
        val contaResponse = itauContasClient.buscaContaPorTipo(novaChavePixRequest.clientId,
                                                        novaChavePixRequest.tipoContaItau)

        val conta = contaResponse.body()?.toModel() ?: throw IllegalStateException("ClienteId '${novaChavePixRequest.clientId}' nao encontrado")

        val chavePix =  chavePixRepository.save(novaChavePixRequest.toChavePix(conta))

        val chavePixBcbRequest = ChavePixBcbRequest(
            keyType = PixKeyTypeBcb.by(chavePix.tipoChavePix),
            key = chavePix.chave,
            bankAccount = BankAccountRequest(
                participant = chavePix.conta.ITAU_UNIBANCO_ISPB,
                branch = chavePix.conta.agencia,
                accountNumber = chavePix.conta.numeroDaConta,
                accountType = when (chavePix.tipoContaItau) {
                    TipoContaItau.CONTA_CORRENTE -> AccountType.CACC
                    TipoContaItau.CONTA_POUPANCA -> AccountType.SVGS
                    else -> AccountType.UNKNOWN
                }
            ),
            owner = OwnerRequest(
                type = TypePerson.NATURAL_PERSON,
                name = chavePix.conta.nomeDoTitular,
                taxIdNumber = chavePix.conta.cpfDoTitular
            )
        )

//        logger.info(chavePixBcbRequest.toString())

        val bcbResponse =  bcbClient.registraChavePix(chavePixBcbRequest)

        if (bcbResponse.status != HttpStatus.CREATED) {
            throw IllegalStateException("Nao foi possivel registrar chave pix no Banco Central")
        }

        chavePix.atualizaChave(bcbResponse.body()!!.key)

        return chavePix
    }

}
