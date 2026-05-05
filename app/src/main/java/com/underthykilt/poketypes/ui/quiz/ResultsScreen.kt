package com.underthykilt.poketypes.ui.quiz

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.underthykilt.poketypes.data.multiplierLabel
import com.underthykilt.poketypes.data.performanceMessage
import com.underthykilt.poketypes.ui.components.TypeBadge
import com.underthykilt.poketypes.ui.theme.CorrectGreen
import com.underthykilt.poketypes.ui.theme.WrongRed
import com.underthykilt.poketypes.ui.theme.scoreColor

@Composable
fun ResultsScreen(
    state: QuizState,
    paddingValues: PaddingValues,
    onPlayAgain: () -> Unit,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val total = state.quizLength ?: state.questions.size
                Text("Quiz Complete!", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                Text(
                    if (state.quizLength != null) "${state.correctAnswers} / $total"
                    else "${state.correctAnswers} correct",
                    fontSize = 52.sp,
                    fontWeight = FontWeight.Bold,
                    color = scoreColor(state.correctAnswers, total)
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    performanceMessage(state.correctAnswers, total),
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                )
            }
        }

        Spacer(Modifier.height(24.dp))

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
            state.questionResults.forEachIndexed { i, correct ->
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(if (correct) CorrectGreen else WrongRed, CircleShape),
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

        val wrongIndices = state.questionResults.indices.filter { !state.questionResults[it] }
        if (wrongIndices.isEmpty()) {
            Text(
                "Perfect — no wrong answers!",
                fontSize = 13.sp,
                color = CorrectGreen,
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
                val wq = state.questions[i]
                val wDef2 = wq.defendingType2
                val isReverse = wq.answerChoices.isNotEmpty()
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(Modifier.fillMaxWidth().padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TypeBadge(wq.attackingType)
                            Text(
                                if (isReverse) "  at ${multiplierLabel(wq.promptMultiplier ?: 1f)}  →  ?"
                                else "  →  ",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            if (!isReverse) {
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
                        }
                        Spacer(Modifier.height(8.dp))
                        if (isReverse) {
                            val userPair = wq.answerChoices.getOrNull(state.userAnswers[i].toInt())
                            val correctPair = wq.answerChoices.getOrNull(wq.correctAnswer.toInt())
                            Row(
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        "Your answer",
                                        fontSize = 10.sp,
                                        color = WrongRed
                                    )
                                    if (userPair != null) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            TypeBadge(userPair.first)
                                            if (userPair.second != null) {
                                                Text(" / ", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                                TypeBadge(userPair.second!!)
                                            }
                                        }
                                    }
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        "Correct",
                                        fontSize = 10.sp,
                                        color = CorrectGreen
                                    )
                                    if (correctPair != null) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            TypeBadge(correctPair.first)
                                            if (correctPair.second != null) {
                                                Text(" / ", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                                TypeBadge(correctPair.second!!)
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
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
                                        multiplierLabel(state.userAnswers[i]),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = WrongRed
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
                                        color = CorrectGreen
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (state.quizLength != null) {
            Spacer(Modifier.height(24.dp))
            Text(
                "Recent Scores",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
            Spacer(Modifier.height(8.dp))
            if (state.history.isEmpty()) {
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
                    state.history.forEachIndexed { i, score ->
                        val isCurrent = i == state.history.size - 1
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .background(scoreColor(score, state.quizLength), CircleShape)
                                .then(
                                    if (isCurrent) Modifier.border(2.5.dp, Color.White, CircleShape)
                                    else Modifier
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
        }

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = onPlayAgain,
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
    }
}
