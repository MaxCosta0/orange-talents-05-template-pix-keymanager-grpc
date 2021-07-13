package br.com.zupacademy.maxley.repository

import br.com.zupacademy.maxley.model.ChavePix
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface ChavePixRepository: JpaRepository<ChavePix, Long> {
    fun findByChave(chave: String?): Optional<ChavePix>
    fun findByIdAndClientId(pixId: Long?, clientId: UUID?): Optional<ChavePix>
}