package com.underthykilt.poketypes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.underthykilt.poketypes.data.AppSettings
import com.underthykilt.poketypes.data.Difficulty
import com.underthykilt.poketypes.data.Generation
import com.underthykilt.poketypes.data.PresentationMode
import com.underthykilt.poketypes.data.QuizLength
import com.underthykilt.poketypes.data.QuizMode
import com.underthykilt.poketypes.data.SettingsRepository
import com.underthykilt.poketypes.data.SpriteGeneration
import com.underthykilt.poketypes.ui.HomeScreen
import com.underthykilt.poketypes.ui.QuizScreen
import com.underthykilt.poketypes.ui.SettingsScreen
import com.underthykilt.poketypes.ui.StudyScreen
import com.underthykilt.poketypes.ui.navigation.HomeRoute
import com.underthykilt.poketypes.ui.navigation.QuizRoute
import com.underthykilt.poketypes.ui.navigation.SettingsRoute
import com.underthykilt.poketypes.ui.navigation.StatsRoute
import com.underthykilt.poketypes.ui.navigation.StudyRoute
import com.underthykilt.poketypes.ui.stats.StatsScreen
import com.underthykilt.poketypes.ui.theme.PokeTypesTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val settingsRepository by lazy { SettingsRepository(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings by settingsRepository.settings.collectAsState(initial = AppSettings())
            val scope = rememberCoroutineScope()

            PokeTypesTheme(darkTheme = settings.isDarkTheme) {
                Surface(Modifier.fillMaxSize()) {
                    val navController = rememberNavController()

                    NavHost(navController, startDestination = HomeRoute) {
                        composable<HomeRoute> {
                            HomeScreen(
                                settings = settings,
                                onStudy = {
                                    navController.navigate(StudyRoute(settings.generation.name))
                                },
                                onStats = { navController.navigate(StatsRoute) },
                                onSettings = { navController.navigate(SettingsRoute) },
                                onSingleQuiz = {
                                    navController.navigate(
                                        QuizRoute(
                                            settings.generation.name,
                                            QuizMode.SINGLE.name,
                                            settings.difficulty.name,
                                            settings.quizLength.name,
                                            settings.presentationMode.name,
                                            settings.spriteGeneration.name,
                                        )
                                    )
                                },
                                onDualQuiz = {
                                    navController.navigate(
                                        QuizRoute(
                                            settings.generation.name,
                                            QuizMode.DOUBLE.name,
                                            settings.difficulty.name,
                                            settings.quizLength.name,
                                            settings.presentationMode.name,
                                            settings.spriteGeneration.name,
                                        )
                                    )
                                },
                                onReverseSingleQuiz = {
                                    navController.navigate(
                                        QuizRoute(
                                            settings.generation.name,
                                            QuizMode.REVERSE_SINGLE.name,
                                            settings.difficulty.name,
                                            settings.quizLength.name,
                                            settings.presentationMode.name,
                                            settings.spriteGeneration.name,
                                        )
                                    )
                                },
                                onReverseDualQuiz = {
                                    navController.navigate(
                                        QuizRoute(
                                            settings.generation.name,
                                            QuizMode.REVERSE_DOUBLE.name,
                                            settings.difficulty.name,
                                            settings.quizLength.name,
                                            settings.presentationMode.name,
                                            settings.spriteGeneration.name,
                                        )
                                    )
                                },
                            )
                        }
                        composable<SettingsRoute> {
                            SettingsScreen(
                                settings = settings,
                                onSetDarkTheme        = { scope.launch { settingsRepository.setDarkTheme(it)          } },
                                onSetGeneration       = { scope.launch { settingsRepository.setGeneration(it)         } },
                                onSetDifficulty       = { scope.launch { settingsRepository.setDifficulty(it)         } },
                                onSetQuizLength       = { scope.launch { settingsRepository.setQuizLength(it)         } },
                                onSetPresentationMode = { scope.launch { settingsRepository.setPresentationMode(it)   } },
                                onSetSpriteGeneration = { scope.launch { settingsRepository.setSpriteGeneration(it)   } },
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable<StatsRoute> {
                            StatsScreen(onBack = { navController.popBackStack() })
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
                                generation        = Generation.valueOf(route.generationName),
                                quizMode          = QuizMode.valueOf(route.quizModeName),
                                difficulty        = Difficulty.valueOf(route.difficultyName),
                                quizLength        = QuizLength.valueOf(route.quizLengthName),
                                presentationMode  = PresentationMode.valueOf(route.presentationModeName),
                                spriteGeneration  = SpriteGeneration.valueOf(route.spriteGenerationName),
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
