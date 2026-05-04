package com.underthykilt.poketypes.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.underthykilt.poketypes.data.Generation
import com.underthykilt.poketypes.data.PokemonType
import com.underthykilt.poketypes.data.availableTypes
import com.underthykilt.poketypes.data.getEffectiveness
import com.underthykilt.poketypes.ui.theme.effectivenessColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyScreen(generation: Generation, onBack: () -> Unit) {
    val types = remember(generation) { availableTypes(generation) }
    var selectedAttacking by remember(generation) { mutableStateOf<PokemonType?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Type Chart · ${generation.label}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            Text(
                "Tap an attacking type to highlight its row:",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
            Row(
                Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                types.forEach { type ->
                    val selected = selectedAttacking == type
                    Box(
                        modifier = Modifier
                            .padding(2.dp)
                            .background(
                                if (selected) type.color else type.color.copy(alpha = 0.5f),
                                MaterialTheme.shapes.small
                            )
                            .border(
                                width = if (selected) 2.dp else 0.dp,
                                color = Color.White,
                                shape = MaterialTheme.shapes.small
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .clickable { selectedAttacking = if (selected) null else type },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(type.displayName, color = Color.White, fontSize = 10.sp)
                    }
                }
            }

            val cellSize = 36.dp
            val labelWidth = 56.dp

            Box(Modifier.fillMaxSize()) {
                Column(
                    Modifier
                        .horizontalScroll(rememberScrollState())
                        .verticalScroll(rememberScrollState())
                ) {
                    Row {
                        Spacer(Modifier.width(labelWidth))
                        types.forEach { def ->
                            Box(
                                Modifier
                                    .size(cellSize)
                                    .background(def.color.copy(alpha = 0.85f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    def.displayName.take(3),
                                    color = Color.White,
                                    fontSize = 7.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }
                            Spacer(Modifier.width(1.dp))
                        }
                    }
                    Spacer(Modifier.height(1.dp))
                    types.forEach { atk ->
                        val isHighlighted = selectedAttacking == atk
                        Row {
                            Box(
                                Modifier
                                    .width(labelWidth)
                                    .height(cellSize)
                                    .background(
                                        if (isHighlighted) atk.color else atk.color.copy(alpha = 0.6f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    atk.displayName.take(5),
                                    color = Color.White,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            types.forEach { def ->
                                val mult = getEffectiveness(atk, def, generation)
                                val cellColor = effectivenessColor(mult)
                                Box(
                                    Modifier
                                        .size(cellSize)
                                        .background(
                                            if (selectedAttacking == null || isHighlighted)
                                                cellColor
                                            else
                                                cellColor.copy(alpha = 0.25f)
                                        )
                                        .border(
                                            width = if (isHighlighted) 1.dp else 0.dp,
                                            color = Color.White.copy(alpha = 0.3f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (mult != 1f) {
                                        Text(
                                            multiplierText(mult),
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Spacer(Modifier.width(1.dp))
                            }
                        }
                        Spacer(Modifier.height(1.dp))
                    }
                }
            }
        }
    }
}

private fun multiplierText(mult: Float): String = when (mult) {
    0f -> "0"
    0.5f -> "½"
    2f -> "2"
    else -> "1"
}
