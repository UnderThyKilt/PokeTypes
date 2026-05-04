package com.underthykilt.poketypes.data

import kotlinx.coroutines.flow.Flow

interface TypeStatRepository {
    suspend fun record(question: QuizQuestion, wasCorrect: Boolean, mode: QuizMode, gen: Generation)
    fun getAttackingAccuracy(): Flow<List<TypeAccuracyRow>>
    fun getDefendingAccuracy(): Flow<List<TypeAccuracyRow>>
}

class RoomTypeStatRepository(private val dao: TypeStatDao) : TypeStatRepository {
    override suspend fun record(question: QuizQuestion, wasCorrect: Boolean, mode: QuizMode, gen: Generation) {
        dao.insert(
            TypeStatEntity(
                attackingType = question.attackingType.name,
                defendingType = question.defendingType.name,
                defendingType2 = question.defendingType2?.name,
                wasCorrect = wasCorrect,
                quizMode = mode.name,
                generation = gen.name,
            )
        )
    }

    override fun getAttackingAccuracy(): Flow<List<TypeAccuracyRow>> = dao.getAttackingAccuracy()
    override fun getDefendingAccuracy(): Flow<List<TypeAccuracyRow>> = dao.getDefendingAccuracy()
}
