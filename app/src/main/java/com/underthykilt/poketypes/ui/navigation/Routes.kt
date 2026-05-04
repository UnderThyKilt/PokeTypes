package com.underthykilt.poketypes.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
object HomeRoute

@Serializable
object StatsRoute

@Serializable
object SettingsRoute

@Serializable
data class StudyRoute(val generationName: String)

@Serializable
data class QuizRoute(val generationName: String, val quizModeName: String, val difficultyName: String, val quizLengthName: String)
