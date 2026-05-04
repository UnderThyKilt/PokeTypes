package com.underthykilt.poketypes.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
object HomeRoute

@Serializable
data class StudyRoute(val generationName: String)

@Serializable
data class QuizRoute(val generationName: String, val quizModeName: String)
