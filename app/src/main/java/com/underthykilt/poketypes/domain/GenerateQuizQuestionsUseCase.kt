package com.underthykilt.poketypes.domain

import com.underthykilt.poketypes.data.Difficulty
import com.underthykilt.poketypes.data.Generation
import com.underthykilt.poketypes.data.PresentationMode
import com.underthykilt.poketypes.data.QuizMode
import com.underthykilt.poketypes.data.QuizQuestion
import com.underthykilt.poketypes.data.SpriteGeneration
import com.underthykilt.poketypes.data.generateQuestion
import com.underthykilt.poketypes.data.questionKey

class GenerateQuizQuestionsUseCase(private val enrichUseCase: EnrichWithPokemonUseCase) {

    fun buildInitialQuestions(
        count: Int,
        gen: Generation,
        mode: QuizMode,
        difficulty: Difficulty,
        presentationMode: PresentationMode,
        spriteGeneration: SpriteGeneration,
        seenKeys: MutableSet<Any>,
    ): List<QuizQuestion> = buildList {
        repeat(count * 50) {
            if (size == count) return@repeat
            val q = generateQuestion(gen, mode, difficulty)
            if (seenKeys.add(questionKey(q)) && enrichUseCase.hasRequiredPokemon(q, presentationMode, spriteGeneration)) {
                add(enrichUseCase.enrich(q, presentationMode, spriteGeneration))
            }
        }
        while (size < count) {
            add(enrichUseCase.enrich(generateQuestion(gen, mode, difficulty), presentationMode, spriteGeneration))
        }
    }

    fun generateNext(
        gen: Generation,
        mode: QuizMode,
        difficulty: Difficulty,
        presentationMode: PresentationMode,
        spriteGeneration: SpriteGeneration,
        seenKeys: MutableSet<Any>,
    ): QuizQuestion {
        repeat(100) {
            val q = generateQuestion(gen, mode, difficulty)
            if (seenKeys.add(questionKey(q)) && enrichUseCase.hasRequiredPokemon(q, presentationMode, spriteGeneration)) {
                return enrichUseCase.enrich(q, presentationMode, spriteGeneration)
            }
        }
        return enrichUseCase.enrich(generateQuestion(gen, mode, difficulty), presentationMode, spriteGeneration)
    }
}
