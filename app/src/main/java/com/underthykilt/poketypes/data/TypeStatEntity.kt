package com.underthykilt.poketypes.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quiz_attempts")
data class TypeStatEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val attackingType: String,
    val defendingType: String,
    val defendingType2: String?,
    val wasCorrect: Boolean,
    val quizMode: String,
    val generation: String,
    val timestamp: Long = System.currentTimeMillis(),
)
