package com.underthykilt.poketypes.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.underthykilt.poketypes.data.AppSettings
import com.underthykilt.poketypes.data.Difficulty
import com.underthykilt.poketypes.data.Generation
import com.underthykilt.poketypes.data.PresentationMode
import com.underthykilt.poketypes.data.QuizLength
import com.underthykilt.poketypes.data.SpriteGeneration
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.Alignment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: AppSettings,
    onSetDarkTheme: (Boolean) -> Unit,
    onSetGeneration: (Generation) -> Unit,
    onSetDifficulty: (Difficulty) -> Unit,
    onSetQuizLength: (QuizLength) -> Unit,
    onSetPresentationMode: (PresentationMode) -> Unit,
    onSetSpriteGeneration: (SpriteGeneration) -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Options") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            SettingsSectionLabel("Appearance")

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Dark Mode", fontSize = 15.sp)
                    Text(
                        if (settings.isDarkTheme) "On" else "Off",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }
                Switch(
                    checked = settings.isDarkTheme,
                    onCheckedChange = onSetDarkTheme,
                )
            }

            SettingsDivider()
            SettingsSectionLabel("Game")

            SettingsLabel("Generation", settings.generation.label)
            Spacer(Modifier.height(8.dp))
            SegmentedSelector(
                options = Generation.entries,
                selected = settings.generation,
                onSelect = onSetGeneration,
                label = { it.label },
                sublabel = { it.description },
            )

            Spacer(Modifier.height(20.dp))

            SettingsLabel("Difficulty", settings.difficulty.label)
            Spacer(Modifier.height(8.dp))
            SegmentedSelector(
                options = Difficulty.entries,
                selected = settings.difficulty,
                onSelect = onSetDifficulty,
                label = { it.label },
                sublabel = { d -> if (d == Difficulty.NORMAL) "10 core types" else "All types" },
            )

            Spacer(Modifier.height(20.dp))

            SettingsLabel("Quiz Length", settings.quizLength.label)
            Spacer(Modifier.height(8.dp))
            SegmentedSelector(
                options = QuizLength.entries,
                selected = settings.quizLength,
                onSelect = onSetQuizLength,
                label = { it.label },
            )

            Spacer(Modifier.height(20.dp))

            SettingsLabel("Style", settings.presentationMode.label)
            Spacer(Modifier.height(8.dp))
            SegmentedSelector(
                options = PresentationMode.entries,
                selected = settings.presentationMode,
                onSelect = onSetPresentationMode,
                label = { it.label },
                sublabel = { if (it == PresentationMode.CLASSIC) "Type badges" else "Pokémon sprites" },
            )

            if (settings.presentationMode == PresentationMode.POKEMON) {
                Spacer(Modifier.height(20.dp))
                SettingsLabel("Pokémon Generation", settings.spriteGeneration.label)
                Spacer(Modifier.height(8.dp))
                SegmentedSelector(
                    options = SpriteGeneration.entries,
                    selected = settings.spriteGeneration,
                    onSelect = onSetSpriteGeneration,
                    label = { it.label },
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SettingsSectionLabel(text: String) {
    Spacer(Modifier.height(16.dp))
    Text(
        text.uppercase(),
        fontSize = 11.sp,
        color = MaterialTheme.colorScheme.primary,
        letterSpacing = 1.sp,
    )
    Spacer(Modifier.height(4.dp))
}

@Composable
private fun SettingsLabel(title: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(title, fontSize = 15.sp, modifier = Modifier.weight(1f))
        Text(value, fontSize = 13.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
    }
}

@Composable
private fun SettingsDivider() {
    Spacer(Modifier.height(12.dp))
    HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f))
    Spacer(Modifier.height(4.dp))
}
