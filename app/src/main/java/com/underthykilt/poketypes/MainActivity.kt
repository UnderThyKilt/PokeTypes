package com.underthykilt.poketypes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.underthykilt.poketypes.data.Generation
import com.underthykilt.poketypes.ui.HomeScreen
import com.underthykilt.poketypes.ui.QuizMode
import com.underthykilt.poketypes.ui.QuizScreen
import com.underthykilt.poketypes.ui.StudyScreen
import com.underthykilt.poketypes.ui.theme.PokeTypesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PokeTypesTheme {
                Surface(Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    var generation by remember { mutableStateOf(Generation.GEN6_PLUS) }
                    var quizMode by remember { mutableStateOf(QuizMode.SINGLE) }

                    NavHost(navController, startDestination = "home") {
                        composable("home") {
                            HomeScreen(
                                generation = generation,
                                onGenerationChange = { generation = it },
                                onStudy = { navController.navigate("study") },
                                onSingleQuiz = {
                                    quizMode = QuizMode.SINGLE
                                    navController.navigate("quiz")
                                },
                                onDualQuiz = {
                                    quizMode = QuizMode.DOUBLE
                                    navController.navigate("quiz")
                                }
                            )
                        }
                        composable("study") {
                            StudyScreen(
                                generation = generation,
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable("quiz") {
                            QuizScreen(
                                generation = generation,
                                quizMode = quizMode,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
