package com.underthykilt.poketypes.domain

import com.underthykilt.poketypes.data.PresentationMode
import com.underthykilt.poketypes.data.QuizQuestion
import com.underthykilt.poketypes.data.SpriteGeneration
import com.underthykilt.poketypes.data.pokemon.PokemonRepository

class EnrichWithPokemonUseCase(private val repository: PokemonRepository) {

    fun enrich(q: QuizQuestion, mode: PresentationMode, spriteGen: SpriteGeneration): QuizQuestion {
        if (mode != PresentationMode.POKEMON) return q
        val maxGen = spriteGen.maxGen
        val isReverse = q.answerChoices.isNotEmpty()
        return q.copy(
            attackingPokemon = repository.randomForTypes(q.attackingType, maxGeneration = maxGen),
            defendingPokemon = if (!isReverse) {
                if (q.defendingType2 != null)
                    repository.randomForTypes(q.defendingType, q.defendingType2, maxGeneration = maxGen)
                else
                    repository.randomForTypes(q.defendingType, maxGeneration = maxGen)
            } else null,
        )
    }

    fun hasRequiredPokemon(q: QuizQuestion, mode: PresentationMode, spriteGen: SpriteGeneration): Boolean {
        if (mode != PresentationMode.POKEMON) return true
        val maxGen = spriteGen.maxGen
        if (!repository.hasForTypes(q.attackingType, maxGeneration = maxGen)) return false
        if (q.answerChoices.isNotEmpty()) return true
        return if (q.defendingType2 != null)
            repository.hasForTypes(q.defendingType, q.defendingType2, maxGeneration = maxGen)
        else
            repository.hasForTypes(q.defendingType, maxGeneration = maxGen)
    }
}
