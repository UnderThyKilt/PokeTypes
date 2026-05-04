# PokeTypes

An Android app for learning Pokémon type matchups. Study the full type chart or test your knowledge with single and dual-type quizzes across three game generations.

---

## Screens

### Home
Entry point. Select a generation (Gen 1, Gen 2–5, Gen 6+), then launch the study chart or either quiz mode.

### Study Chart
An 18×18 scrollable grid of type matchups. Tap any attacking type to highlight its row. Cells are color-coded:
- Green — super effective (2×)
- Red — not very effective (½×)
- Dark — no effect (0×)
- Neutral — normal (1×)

### Quiz
Two modes:

| Mode | Defending types | Answer choices |
|------|----------------|----------------|
| Single Type | 1 | 0×, ½×, 1×, 2× |
| Dual Type | 2 | 0×, ¼×, ½×, 1×, 2×, 4× |

Each quiz is 10 questions generated upfront. Correct answers turn green, wrong answers turn red. A streak counter appears on consecutive correct answers. The results screen shows your score, a per-question dot summary, a review of every missed question, and your last 10 scores for that mode (persisted across sessions).

---

## Generation Differences

| Generation | Change |
|-----------|--------|
| Gen 1 | Ghost → Psychic is 0×; Poison → Bug and Bug → Poison are 2×; no Steel, Dark, or Fairy types |
| Gen 2–5 | Steel resists Ghost and Dark; Dark and Steel types added |
| Gen 6+ | Full modern chart; Steel no longer resists Ghost/Dark; Fairy type added |

---

## Project Structure

```
app/src/main/java/com/underthykilt/poketypes/
├── MainActivity.kt          # Single activity, NavHost, top-level generation/mode state
├── data/
│   ├── TypeChart.kt         # PokemonType enum, Generation enum, type chart logic
│   └── ScoreHistory.kt      # SharedPreferences score persistence (up to 10 per mode)
└── ui/
    ├── HomeScreen.kt        # Generation selector, mode buttons, SegmentedSelector<T>
    ├── StudyScreen.kt       # 18×18 scrollable type chart grid
    ├── QuizScreen.kt        # Quiz engine, results screen, TypeBadge composable
    └── theme/Theme.kt       # Dark navy color scheme

docs/
└── ARCHITECTURE.md          # Architecture review and proposed improvements
```

### Key types

- **`PokemonType`** — enum of all 18 types, each with a `displayName` and `color`
- **`Generation`** — `GEN1`, `GEN2_5`, `GEN6_PLUS`; drives which chart is used
- **`QuizMode`** — `SINGLE` or `DOUBLE`
- **`QuizQuestion`** — `attackingType`, `defendingType`, optional `defendingType2`, `correctAnswer: Float`
- **`ScoreHistory`** — singleton; saves/loads up to 10 scores per quiz mode via `SharedPreferences`

### Key functions

- `getEffectiveness(attacking, defending, gen): Float` — core chart lookup; returns 0f, 0.5f, 1f, 2f, or 4f (`TypeChart.kt`)
- `availableTypes(gen)` — filters out types that didn't exist in the selected generation (`TypeChart.kt`)
- `multiplierLabel(mult)` — converts a float to a display string like `"½×"` or `"4×"` (`TypeChart.kt`)
- `generateQuestion(gen, mode)` — picks random types and computes the correct multiplier (`QuizScreen.kt`)
- `SegmentedSelector<T>()` — generic reusable toggler used for the generation and mode selectors (`HomeScreen.kt`)

---

## Tech Stack

| Component | Library / Version |
|-----------|------------------|
| Language | Kotlin 2.0.21 |
| UI | Jetpack Compose (BOM 2024.10.01) |
| Material Design | Material3 |
| Navigation | Navigation Compose 2.8.4 |
| Persistence | SharedPreferences |
| Min SDK | 26 (Android 8.0) |
| Target / Compile SDK | 37 (Android 14) |

---

## Build

```bash
./gradlew assembleDebug
```

All type data is hardcoded — no network access required.
