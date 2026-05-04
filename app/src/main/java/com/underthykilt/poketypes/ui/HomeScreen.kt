package com.underthykilt.poketypes.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.underthykilt.poketypes.data.AppSettings
import com.underthykilt.poketypes.ui.theme.QuizDualColor

@Composable
fun HomeScreen(
    settings: AppSettings,
    onStudy: () -> Unit,
    onStats: () -> Unit,
    onSettings: () -> Unit,
    onSingleQuiz: () -> Unit,
    onDualQuiz: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp, vertical = 16.dp)) {
        TextButton(
            onClick = onStats,
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            Text("Stats", fontSize = 13.sp)
        }
        TextButton(
            onClick = onSettings,
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Text("Options", fontSize = 13.sp)
        }

        Column(
            modifier = Modifier.align(Alignment.Center).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "PokeTypes",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Master the type chart",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "${settings.generation.label}  ·  ${settings.difficulty.label}  ·  ${settings.quizLength.label}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f)
            )

            Spacer(Modifier.height(40.dp))

            Button(
                onClick = onStudy,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("Study Chart", fontSize = 18.sp)
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onSingleQuiz,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Single Type Quiz", fontSize = 18.sp)
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onDualQuiz,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = QuizDualColor)
            ) {
                Text("Dual Type Quiz", fontSize = 18.sp)
            }
        }
    }
}

@Composable
fun <T> SegmentedSelector(
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    label: (T) -> String,
    sublabel: ((T) -> String)? = null,
) {
    val shape = RoundedCornerShape(8.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .border(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f), shape)
    ) {
        options.forEachIndexed { index, option ->
            val isSelected = option == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                    )
                    .clickable { onSelect(option) }
                    .padding(vertical = 10.dp, horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        label(option),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White
                                else MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                    if (sublabel != null) {
                        Text(
                            sublabel(option),
                            fontSize = 9.sp,
                            color = if (isSelected) Color.White.copy(alpha = 0.8f)
                                    else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            if (index < options.size - 1) {
                Box(
                    Modifier.width(1.dp).height(48.dp)
                        .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f))
                )
            }
        }
    }
}
