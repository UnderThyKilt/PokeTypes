package com.underthykilt.poketypes.ui.quiz

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.underthykilt.poketypes.data.DataStoreScoreRepository
import com.underthykilt.poketypes.data.Generation
import com.underthykilt.poketypes.data.QuizMode
import com.underthykilt.poketypes.data.QuizQuestion
import com.underthykilt.poketypes.data.QUIZ_LENGTH
import com.underthykilt.poketypes.data.ScoreRepository
import com.underthykilt.poketypes.data.generateQuestion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class QuizState(
    val questions: List<QuizQuestion> = emptyList(),
    val questionIndex: Int = 0,
    val selected: Float? = null,
    val correctAnswers: Int = 0,
    val streak: Int = 0,
    val questionResults: List<Boolean> = emptyList(),
    val userAnswers: List<Float> = emptyList(),
    val quizComplete: Boolean = false,
    val history: List<Int> = emptyList(),
)

class QuizViewModel(
    application: Application,
    val generation: Generation,
    val quizMode: QuizMode,
    private val repository: ScoreRepository,
) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(newRound())
    val state: StateFlow<QuizState> = _state.asStateFlow()

    init {
        refreshHistory()
    }

    private fun newRound(): QuizState =
        QuizState(questions = List(QUIZ_LENGTH) { generateQuestion(generation, quizMode) })

    private fun refreshHistory() {
        viewModelScope.launch {
            val history = repository.loadScores(quizMode.name).first()
            _state.update { it.copy(history = history) }
        }
    }

    fun selectAnswer(choice: Float) {
        val s = _state.value
        if (s.selected != null) return
        val isCorrect = choice == s.questions[s.questionIndex].correctAnswer
        _state.update {
            it.copy(
                selected = choice,
                correctAnswers = if (isCorrect) it.correctAnswers + 1 else it.correctAnswers,
                streak = if (isCorrect) it.streak + 1 else 0,
            )
        }
    }

    fun advance() {
        val s = _state.value
        val answer = s.selected ?: return
        val wasCorrect = answer == s.questions[s.questionIndex].correctAnswer
        val newResults = s.questionResults + wasCorrect
        val newAnswers = s.userAnswers + answer
        val isLast = s.questionIndex == QUIZ_LENGTH - 1

        if (isLast) {
            viewModelScope.launch {
                repository.saveScore(quizMode.name, s.correctAnswers)
                val newHistory = repository.loadScores(quizMode.name).first()
                _state.update {
                    it.copy(
                        questionResults = newResults,
                        userAnswers = newAnswers,
                        quizComplete = true,
                        history = newHistory,
                    )
                }
            }
        } else {
            _state.update {
                it.copy(
                    questionResults = newResults,
                    userAnswers = newAnswers,
                    questionIndex = it.questionIndex + 1,
                    selected = null,
                )
            }
        }
    }

    fun reset() {
        _state.value = newRound()
        refreshHistory()
    }

    companion object {
        fun factory(application: Application, generation: Generation, quizMode: QuizMode): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    QuizViewModel(application, generation, quizMode, DataStoreScoreRepository(application))
                }
            }
    }
}
