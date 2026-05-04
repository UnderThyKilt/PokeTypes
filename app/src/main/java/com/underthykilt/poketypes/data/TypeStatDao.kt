package com.underthykilt.poketypes.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

data class TypeAccuracyRow(val typeName: String, val correct: Int, val total: Int)

@Dao
interface TypeStatDao {
    @Insert
    suspend fun insert(entity: TypeStatEntity)

    @Query("""
        SELECT attackingType AS typeName,
               SUM(CASE WHEN wasCorrect = 1 THEN 1 ELSE 0 END) AS correct,
               COUNT(*) AS total
        FROM quiz_attempts
        GROUP BY attackingType
    """)
    fun getAttackingAccuracy(): Flow<List<TypeAccuracyRow>>

    @Query("""
        SELECT defendingType AS typeName,
               SUM(CASE WHEN wasCorrect = 1 THEN 1 ELSE 0 END) AS correct,
               COUNT(*) AS total
        FROM quiz_attempts
        GROUP BY defendingType
    """)
    fun getDefendingAccuracy(): Flow<List<TypeAccuracyRow>>
}
