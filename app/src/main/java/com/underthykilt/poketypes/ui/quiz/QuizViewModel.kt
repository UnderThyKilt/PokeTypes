package com.underthykilt.poketypes.ui.quiz

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.underthykilt.poketypes.PokeTypesApp
import com.underthykilt.poketypes.data.DataStoreScoreRepository
import com.underthykilt.poketypes.data.Difficulty
import com.underthykilt.poketypes.data.Generation
import com.underthykilt.poketypes.data.PresentationMode
import com.underthykilt.poketypes.data.QuizLength
import com.underthykilt.poketypes.data.QuizMode
import com.underthykilt.poketypes.data.SpriteGeneration
import com.underthykilt.poketypes.data.QuizQuestion
import com.underthykilt.poketypes.data.RoomTypeStatRepository
import com.underthykilt.poketypes.data.ScoreRepository
import com.underthykilt.poketypes.data.TypeStatDatabase
import com.underthykilt.poketypes.data.TypeStatRepository
import com.underthykilt.poketypes.domain.EnrichWithPokemonUseCase
import com.underthykilt.poketypes.domain.GenerateQuizQuestionsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class QuizState(
    val quizLength: Int? = 10,       // null = endless
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
    val difficulty: Difficulty,
    val quizLengthSetting: QuizLength,
    val presentationMode: PresentationMode,
    val spriteGeneration: SpriteGeneration,
    private val repository: ScoreRepository,
    private val statRepository: TypeStatRepository,
    private val generateQuestions: GenerateQuizQuestionsUseCase,
) : AndroidViewModel(application) {

    private val quizLength: Int? = quizLengthSetting.count
    private val seenKeys = mutableSetOf<Any>()

    private val _state = MutableStateFlow(newRound())
    val state: StateFlow<QuizState> = _state.asStateFlow()

    init {
        if (quizLength != null) refreshHistory()
    }

    private fun newRound(): QuizState {
        seenKeys.clear()
        val count = quizLength ?: 1
        val questions = generateQuestions.buildInitialQuestions(
            count, generation, quizMode, difficulty, presentationMode, spriteGeneration, seenKeys
        )
        return QuizState(quizLength = quizLength, questions = questions)
    }

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

    fun advance() = doAdvance(endQuiz = false)
    fun endQuiz() = doAdvance(endQuiz = true)

    private fun doAdvance(endQuiz: Boolean) {
        val s = _state.value
        val answer = s.selected ?: return
        val question = s.questions[s.questionIndex]
        val wasCorrect = answer == question.correctAnswer
        val newResults = s.questionResults + wasCorrect
        val newAnswers = s.userAnswers + answer
        val isLast = when {
            endQuiz -> true
            s.quizLength != null -> s.questionIndex == s.quizLength - 1
            else -> false
        }

        viewModelScope.launch {
            statRepository.record(question, wasCorrect, quizMode, generation)
            if (isLast) {
                if (s.quizLength != null) {
                    repository.saveScore(quizMode.name, s.correctAnswers)
                }
                val newHistory = if (s.quizLength != null)
                    repository.loadScores(quizMode.name).first()
                else
                    emptyList()
                _state.update {
                    it.copy(
                        questionResults = newResults,
                        userAnswers = newAnswers,
                        quizComplete = true,
                        history = newHistory,
                    )
                }
            } else {
                val newQuestions = if (s.quizLength == null)
                    s.questions + generateQuestions.generateNext(
                        generation, quizMode, difficulty, presentationMode, spriteGeneration, seenKeys
                    )
                else
                    s.questions
                _state.update {
                    it.copy(
                        questions = newQuestions,
                        questionResults = newResults,
                        userAnswers = newAnswers,
                        questionIndex = it.questionIndex + 1,
                        selected = null,
                    )
                }
            }
        }
    }

    fun reset() {
        _state.value = newRound()
        if (quizLength != null) refreshHistory()
    }

    companion object {
        fun factory(
            application: Application,
            generation: Generation,
            quizMode: QuizMode,
            difficulty: Difficulty,
            quizLength: QuizLength,
            presentationMode: PresentationMode,
            spriteGeneration: SpriteGeneration,
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = application as PokeTypesApp
                val enrichUseCase = EnrichWithPokemonUseCase(app.pokemonRepository)
                QuizViewModel(
                    application, generation, quizMode, difficulty, quizLength, presentationMode, spriteGeneration,
                    DataStoreScoreRepository(application),
                    RoomTypeStatRepository(TypeStatDatabase.get(application).typeStatDao()),
                    GenerateQuizQuestionsUseCase(enrichUseCase),
                )
            }
        }
    }
}
