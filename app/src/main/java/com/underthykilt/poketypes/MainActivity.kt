package com.underthykilt.poketypes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.underthykilt.poketypes.data.Generation
import com.underthykilt.poketypes.data.QuizMode
import com.underthykilt.poketypes.ui.HomeScreen
import com.underthykilt.poketypes.ui.QuizScreen
import com.underthykilt.poketypes.ui.StudyScreen
import com.underthykilt.poketypes.ui.navigation.HomeRoute
import com.underthykilt.poketypes.ui.navigation.QuizRoute
import com.underthykilt.poketypes.ui.navigation.StudyRoute
import com.underthykilt.poketypes.ui.theme.PokeTypesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PokeTypesTheme {
                Surface(Modifier.fillMaxSize()) {
                    val navController = rememberNavController()

                    NavHost(navController, startDestination = HomeRoute) {
                        composable<HomeRoute> {
                            HomeScreen(
                                onStudy = { gen -> navController.navigate(StudyRoute(gen.name)) },
                                onSingleQuiz = { gen ->
                                    navController.navigate(QuizRoute(gen.name, QuizMode.SINGLE.name))
                                },
                                onDualQuiz = { gen ->
                                    navController.navigate(QuizRoute(gen.name, QuizMode.DOUBLE.name))
                                }
                            )
                        }
                        composable<StudyRoute> { backStackEntry ->
                            val route = backStackEntry.toRoute<StudyRoute>()
                            StudyScreen(
                                generation = Generation.valueOf(route.generationName),
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable<QuizRoute> { backStackEntry ->
                            val route = backStackEntry.toRoute<QuizRoute>()
                            QuizScreen(
                                generation = Generation.valueOf(route.generationName),
                                quizMode = QuizMode.valueOf(route.quizModeName),
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
