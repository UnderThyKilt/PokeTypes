package com.underthykilt.poketypes.ui

import android.app.Application
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.underthykilt.poketypes.data.Difficulty
import com.underthykilt.poketypes.data.Generation
import com.underthykilt.poketypes.data.QuizLength
import com.underthykilt.poketypes.data.QuizMode
import com.underthykilt.poketypes.ui.quiz.QuizContent
import com.underthykilt.poketypes.ui.quiz.QuizViewModel
import com.underthykilt.poketypes.ui.quiz.ResultsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    generation: Generation,
    quizMode: QuizMode,
    difficulty: Difficulty,
    quizLength: QuizLength,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val viewModel: QuizViewModel = viewModel(
        factory = QuizViewModel.factory(
            context.applicationContext as Application,
            generation, quizMode, difficulty, quizLength
        )
    )
    val state by viewModel.state.collectAsState()
    val isEndless = state.quizLength == null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${generation.label} · ${quizMode.label}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (!state.quizComplete) {
                        val label = if (isEndless)
                            "Q ${state.questionIndex + 1}"
                        else
                            "Q ${state.questionIndex + 1} / ${state.quizLength}"
                        Text(
                            label,
                            modifier = Modifier.padding(end = 16.dp),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (state.quizComplete) {
            ResultsScreen(
                state = state,
                paddingValues = padding,
                onPlayAgain = { viewModel.reset() },
                onBack = onBack
            )
        } else {
            QuizContent(
                state = state,
                quizMode = quizMode,
                paddingValues = padding,
                onSelectAnswer = viewModel::selectAnswer,
                onAdvance = viewModel::advance,
                onEndQuiz = viewModel::endQuiz,
            )
        }
    }
}
