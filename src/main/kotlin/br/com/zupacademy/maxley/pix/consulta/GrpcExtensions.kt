package br.com.zupacademy.maxley.pix.consulta

import br.com.zupacademy.maxley.ConsultaChavePixRequest
import javax.validation.ConstraintViolationException
import javax.validation.Validator


fun ConsultaChavePixRequest.toFiltro(validator: Validator): Filtro {
    val filtro = when (filtroCase!!) {
        ConsultaChavePixRequest.FiltroCase.PIXID -> this.pixId.let {
            try {
                Filtro.PorPixId(clientId = it.clientId, pixId = it.pixId)
            } catch (ex: Exception) {
                Filtro.Invalido()
            }
        }
        ConsultaChavePixRequest.FiltroCase.CHAVE -> Filtro.PorChave(this.chave)
        ConsultaChavePixRequest.FiltroCase.FILTRO_NOT_SET -> Filtro.Invalido()
    }

    val violations = validator.validate(filtro)
    if (violations.isNotEmpty()) {
        throw ConstraintViolationException(violations)
    }

    return filtro
}


