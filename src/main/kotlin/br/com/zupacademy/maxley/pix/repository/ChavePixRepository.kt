package br.com.zupacademy.maxley.pix.repository

import br.com.zupacademy.maxley.pix.model.ChavePix
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface ChavePixRepository: JpaRepository<ChavePix, Long> {
    fun findByChave(chave: String?): Optional<ChavePix>
}