package com.underthykilt.poketypes.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.underthykilt.poketypes.data.Generation
import com.underthykilt.poketypes.data.PokemonType
import com.underthykilt.poketypes.data.QUIZ_LENGTH
import com.underthykilt.poketypes.data.ScoreHistory
import com.underthykilt.poketypes.data.availableTypes
import com.underthykilt.poketypes.data.getEffectiveness
import com.underthykilt.poketypes.data.multiplierLabel

enum class QuizMode(val label: String, val description: String) {
    SINGLE("Single Type", "One defending type"),
    DOUBLE("Dual Type", "Two defending types")
}

private val SINGLE_CHOICES = listOf(0f, 0.5f, 1f, 2f)
private val DOUBLE_CHOICES = listOf(0f, 0.25f, 0.5f, 1f, 2f, 4f)

internal val CORRECT_GREEN = Color(0xFF1A6B1A)
internal val WRONG_RED = Color(0xFF8B1A1A)

data class QuizQuestion(
    val attackingType: PokemonType,
    val defendingType: PokemonType,
    val defendingType2: PokemonType? = null,
    val correctAnswer: Float,
)

fun generateQuestion(gen: Generation, mode: QuizMode): QuizQuestion {
    val types = availableTypes(gen)
    val atk = types.random()
    val def = types.random()
    return when (mode) {
        QuizMode.SINGLE -> QuizQuestion(atk, def, null, getEffectiveness(atk, def, gen))
        QuizMode.DOUBLE -> {
            var def2 = types.random()
            while (def2 == def) def2 = types.random()
            val combined = getEffectiveness(atk, def, gen) * getEffectiveness(atk, def2, gen)
            QuizQuestion(atk, def, def2, combined)
        }
    }
}

private fun scoreColor(score: Int): Color = when {
    score >= 9 -> CORRECT_GREEN
    score >= 7 -> Color(0xFFC8960C)
    score >= 5 -> Color(0xFFE65100)
    else -> WRONG_RED
}

private fun performanceMessage(score: Int): String = when {
    score == QUIZ_LENGTH -> "Perfect!"
    score >= 9 -> "Outstanding!"
    score >= 7 -> "Great job!"
    score >= 5 -> "Keep it up!"
    score >= 3 -> "Keep practicing!"
    else -> "Study the chart!"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(generation: Generation, quizMode: QuizMode, onBack: () -> Unit) {
    val context = LocalContext.current
    var resetKey by remember { mutableStateOf(0) }

    // Pre-generate all 10 questions for this round
    val questions = remember(generation, quizMode, resetKey) {
        List(QUIZ_LENGTH) { generateQuestion(generation, quizMode) }
    }
    var questionIndex by remember(generation, quizMode, resetKey) { mutableStateOf(0) }
    var selected by remember(generation, quizMode, resetKey) { mutableStateOf<Float?>(null) }
    var correctAnswers by remember(generation, quizMode, resetKey) { mutableStateOf(0) }
    var streak by remember(generation, quizMode, resetKey) { mutableStateOf(0) }
    var questionResults by remember(generation, quizMode, resetKey) { mutableStateOf(emptyList<Boolean>()) }
    var userAnswers by remember(generation, quizMode, resetKey) { mutableStateOf(emptyList<Float>()) }
    var quizComplete by remember(generation, quizMode, resetKey) { mutableStateOf(false) }
    var history by remember(generation, quizMode) {
        mutableStateOf(ScoreHistory.load(context, quizMode.name))
    }

    val q = questions[questionIndex]
    val choices = if (quizMode == QuizMode.SINGLE) SINGLE_CHOICES else DOUBLE_CHOICES
    val answered = selected != null
    val answeredCorrectly = selected == q.correctAnswer
    val isLastQuestion = questionIndex == QUIZ_LENGTH - 1

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${generation.label} · ${quizMode.label}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (!quizComplete) {
                        Text(
                            "Q ${questionIndex + 1} / $QUIZ_LENGTH",
                            modifier = Modifier.padding(end = 16.dp),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (quizComplete) {
                // ── Results screen ──────────────────────────────────────
                Spacer(Modifier.height(8.dp))

                // Score card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Quiz Complete!", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "$correctAnswers / $QUIZ_LENGTH",
                            fontSize = 52.sp,
                            fontWeight = FontWeight.Bold,
                            color = scoreColor(correctAnswers)
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            performanceMessage(correctAnswers),
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Per-question result dots
                Text(
                    "This Quiz",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    questionResults.forEachIndexed { i, correct ->
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(if (correct) CORRECT_GREEN else WRONG_RED, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "${i + 1}",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Wrong answers review
                val wrongIndices = questionResults.indices.filter { !questionResults[it] }
                if (wrongIndices.isEmpty()) {
                    Text(
                        "Perfect — no wrong answers!",
                        fontSize = 13.sp,
                        color = CORRECT_GREEN,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        "Wrong Answers (${wrongIndices.size})",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                    Spacer(Modifier.height(8.dp))
                    wrongIndices.forEach { i ->
                        val wq = questions[i]
                        val wDef2 = wq.defendingType2
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(Modifier.fillMaxWidth().padding(12.dp)) {
                                // Type matchup
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    TypeBadge(wq.attackingType)
                                    Text(
                                        "  →  ",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                    TypeBadge(wq.defendingType)
                                    if (wDef2 != null) {
                                        Text(
                                            " / ",
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                        TypeBadge(wDef2)
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                                // Answer comparison
                                Row(
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            "Your answer",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                        Text(
                                            multiplierLabel(userAnswers[i]),
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = WRONG_RED
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            "Correct",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                        Text(
                                            multiplierLabel(wq.correctAnswer),
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CORRECT_GREEN
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // History row
                Text(
                    "Recent Scores",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
                Spacer(Modifier.height(8.dp))
                if (history.isEmpty()) {
                    Text(
                        "No previous scores",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.35f)
                    )
                } else {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        history.forEachIndexed { i, score ->
                            val isCurrent = i == history.size - 1
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(scoreColor(score), CircleShape)
                                    .then(
                                        if (isCurrent)
                                            Modifier.border(2.5.dp, Color.White, CircleShape)
                                        else
                                            Modifier
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "$score",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))

                Button(
                    onClick = { resetKey++ },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Play Again", fontSize = 16.sp)
                }
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text("Back to Home", fontSize = 16.sp)
                }

            } else {
                // ── Quiz screen ─────────────────────────────────────────

                // Streak row (fixed height so it never shifts layout)
                Box(
                    modifier = Modifier.fillMaxWidth().height(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (streak > 1) {
                        Text(
                            "🔥 $streak streak!",
                            color = Color(0xFFF08030),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Question card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "How effective is",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Spacer(Modifier.height(12.dp))
                        TypeBadge(q.attackingType, large = true)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "attacking",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Spacer(Modifier.height(8.dp))
                        val def2 = q.defendingType2
                        if (def2 != null) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TypeBadge(q.defendingType, large = true)
                                Text(
                                    " / ",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                                TypeBadge(def2, large = true)
                            }
                        } else {
                            TypeBadge(q.defendingType, large = true)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("?", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Answer choices
                choices.forEach { choice ->
                    val isCorrect = choice == q.correctAnswer
                    val isSelected = choice == selected

                    val containerColor = when {
                        !answered -> MaterialTheme.colorScheme.surface
                        isCorrect -> CORRECT_GREEN
                        isSelected && !isCorrect -> WRONG_RED
                        else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                    }

                    Button(
                        onClick = {
                            if (selected == null) {
                                selected = choice
                                if (isCorrect) { correctAnswers++; streak++ } else streak = 0
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = containerColor),
                        enabled = !answered || isCorrect || isSelected
                    ) {
                        Text(multiplierLabel(choice), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Next / Finish button
                AnimatedVisibility(visible = answered) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Spacer(Modifier.height(12.dp))
                        if (!answeredCorrectly) {
                            val def2 = q.defendingType2
                            val defLabel = def2
                                ?.let { "${q.defendingType.displayName}/${it.displayName}" }
                                ?: q.defendingType.displayName
                            Text(
                                "${q.attackingType.displayName} vs $defLabel = ${multiplierLabel(q.correctAnswer)}",
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                        Button(
                            onClick = {
                                val answer = selected!!
                                val wasCorrect = answer == q.correctAnswer
                                questionResults = questionResults + wasCorrect
                                userAnswers = userAnswers + answer
                                if (isLastQuestion) {
                                    ScoreHistory.save(context, quizMode.name, correctAnswers)
                                    history = ScoreHistory.load(context, quizMode.name)
                                    quizComplete = true
                                } else {
                                    questionIndex++
                                    selected = null
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (answeredCorrectly) CORRECT_GREEN
                                                else MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(
                                if (isLastQuestion) "Finish Quiz" else "Next Question",
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Progress dots (one per question, colored as answered)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(QUIZ_LENGTH) { i ->
                        val dotColor = when {
                            i < questionResults.size -> if (questionResults[i]) CORRECT_GREEN else WRONG_RED
                            i == questionIndex && answered ->
                                if (answeredCorrectly) CORRECT_GREEN else WRONG_RED
                            else -> Color.Gray.copy(alpha = 0.35f)
                        }
                        Box(
                            Modifier
                                .size(10.dp)
                                .background(dotColor, CircleShape)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TypeBadge(type: PokemonType, large: Boolean = false) {
    Box(
        modifier = Modifier
            .background(type.color, MaterialTheme.shapes.medium)
            .padding(
                horizontal = if (large) 20.dp else 8.dp,
                vertical = if (large) 10.dp else 4.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            type.displayName,
            color = Color.White,
            fontSize = if (large) 20.sp else 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
