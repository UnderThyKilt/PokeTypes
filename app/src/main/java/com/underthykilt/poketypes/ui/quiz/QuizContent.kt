package com.underthykilt.poketypes.ui.quiz

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.underthykilt.poketypes.data.PokemonType
import com.underthykilt.poketypes.data.QuizMode
import com.underthykilt.poketypes.data.multiplierLabel
import com.underthykilt.poketypes.data.pokemon.PokemonEntry
import com.underthykilt.poketypes.ui.components.TypeBadge
import com.underthykilt.poketypes.ui.theme.CorrectGreen
import com.underthykilt.poketypes.ui.theme.WrongRed

private val SINGLE_CHOICES = listOf(0f, 0.5f, 1f, 2f)
private val DOUBLE_CHOICES = listOf(0f, 0.25f, 0.5f, 1f, 2f, 4f)

@Composable
fun QuizContent(
    state: QuizState,
    quizMode: QuizMode,
    paddingValues: PaddingValues,
    onSelectAnswer: (Float) -> Unit,
    onAdvance: () -> Unit,
    onEndQuiz: () -> Unit,
) {
    val q = state.questions[state.questionIndex]
    val choices = if (quizMode == QuizMode.SINGLE) SINGLE_CHOICES else DOUBLE_CHOICES
    val answered = state.selected != null
    val answeredCorrectly = state.selected == q.correctAnswer
    val isEndless = state.quizLength == null
    val isLastQuestion = !isEndless && state.questionIndex == (state.quizLength ?: 0) - 1
    val pokemonMode = q.attackingPokemon != null

    Column(
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().height(24.dp),
            contentAlignment = Alignment.Center
        ) {
            if (state.streak > 1) {
                Text(
                    "🔥 ${state.streak} streak!",
                    color = Color(0xFFF08030),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(Modifier.height(8.dp))

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
                if (pokemonMode && q.attackingPokemon != null) {
                    PokemonCard(q.attackingPokemon, q.attackingType)
                } else {
                    TypeBadge(q.attackingType, large = true)
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "attacking",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(Modifier.height(8.dp))
                val def2 = q.defendingType2
                if (def2 != null) {
                    if (pokemonMode && q.defendingPokemon != null) {
                        PokemonCard(q.defendingPokemon, q.defendingType, def2)
                    } else {
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
                    }
                } else {
                    if (pokemonMode && q.defendingPokemon != null) {
                        PokemonCard(q.defendingPokemon, q.defendingType)
                    } else {
                        TypeBadge(q.defendingType, large = true)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text("?", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(16.dp))

        choices.forEach { choice ->
            val isCorrect = choice == q.correctAnswer
            val isSelected = choice == state.selected

            val containerColor = when {
                !answered -> MaterialTheme.colorScheme.surface
                isCorrect -> CorrectGreen
                isSelected && !isCorrect -> WrongRed
                else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
            }

            Button(
                onClick = { onSelectAnswer(choice) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = containerColor),
                enabled = !answered || isCorrect || isSelected
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        multiplierLabel(choice),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        style = TextStyle(drawStyle = Stroke(width = 6f)),
                        color = MaterialTheme.colorScheme.surface
                    )
                    Text(
                        multiplierLabel(choice),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

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
                    onClick = onAdvance,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (answeredCorrectly) CorrectGreen
                        else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        if (isLastQuestion) "Finish Quiz" else "Next Question",
                        fontSize = 16.sp
                    )
                }
                if (isEndless) {
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onEndQuiz,
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("End Quiz", fontSize = 15.sp)
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        if (!isEndless) {
            val quizLen = state.quizLength ?: 0
            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(quizLen) { i ->
                    val dotColor = when {
                        i < state.questionResults.size -> if (state.questionResults[i]) CorrectGreen else WrongRed
                        i == state.questionIndex && answered ->
                            if (answeredCorrectly) CorrectGreen else WrongRed
                        else -> Color.Gray.copy(alpha = 0.35f)
                    }
                    Box(Modifier.size(if (quizLen > 10) 7.dp else 10.dp).background(dotColor, CircleShape))
                }
            }
        }
    }
}

@Composable
private fun PokemonCard(pokemon: PokemonEntry, type: PokemonType, type2: PokemonType? = null) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        AsyncImage(
            model = pokemon.spriteUrl,
            contentDescription = pokemon.name,
            modifier = Modifier.size(52.dp),
        )
        Spacer(Modifier.width(6.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (type2 != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TypeBadge(type, large = true)
                    Text(
                        " / ",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    )
                    TypeBadge(type2, large = true)
                }
            } else {
                TypeBadge(type, large = true)
            }
            Text(
                pokemon.name,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
            )
        }
    }
}
