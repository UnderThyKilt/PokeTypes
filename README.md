# PokeTypes

An Android app for learning Pokémon type matchups. Study the full type chart, test your knowledge with single and dual-type quizzes across three game generations, and track your per-type accuracy over time.

---

## Screens

### Home
Entry point. Shows the current settings summary and launches any mode.

### Options
Persistent settings screen:
- **Dark Mode** toggle
- **Generation** — Gen 1 / Gen 2–5 / Gen 6+
- **Difficulty** — Normal (10 core types) / Hard (all types for the selected generation)
- **Quiz Length** — 5, 10, 20, or Endless
- **Style** — Classic (type badges) or Pokémon (sprites + name + badge)

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

Questions are generated without duplicates within a session. Correct answers turn green, wrong answers turn red. A streak counter appears on consecutive correct answers.

In **Classic** style, questions show colored type badges. In **Pokémon** style, questions show a Pokémon sprite, name, and small type badge instead. Sprites are downloaded on first use and cached persistently — no internet required after that.

The results screen shows your score, a per-question dot summary (for fixed-length quizzes), a review of every missed question, and your last 10 scores for that mode.

**Endless mode** generates questions indefinitely. Tap "End Quiz" at any time; the final score is shown but not saved to history.

### Stats
Per-type accuracy bars for attacking and defending matchups, sorted worst-first. Backed by Room — every answered question is recorded.

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
├── PokeTypesApp.kt          # Application subclass; configures Coil with persistent sprite cache
├── MainActivity.kt          # Single activity, NavHost, settings state
├── data/
│   ├── TypeChart.kt         # PokemonType enum, Generation enum, type chart logic
│   ├── QuizLogic.kt         # QuizQuestion, generateQuestion(), performanceMessage()
│   ├── Difficulty.kt        # Difficulty enum + filteredTypes()
│   ├── QuizLength.kt        # QuizLength enum (FIVE / TEN / TWENTY / ENDLESS)
│   ├── PresentationMode.kt  # PresentationMode enum (CLASSIC / POKEMON)
│   ├── SettingsRepository.kt # All user settings persisted in DataStore
│   ├── ScoreRepository.kt   # ScoreRepository interface + DataStoreScoreRepository
│   ├── TypeStatEntity.kt    # Room entity: one row per answered question
│   ├── TypeStatDao.kt       # Room DAO + TypeAccuracyRow projection
│   ├── TypeStatDatabase.kt  # RoomDatabase singleton
│   ├── TypeStatRepository.kt# TypeStatRepository interface + RoomTypeStatRepository
│   └── pokemon/
│       ├── PokemonEntry.kt  # PokemonEntry data class + spriteUrl property
│       └── TypePokemon.kt   # ~7 Pokémon per type; randomPokemonForType()
└── ui/
    ├── HomeScreen.kt        # Mode buttons, settings summary, SegmentedSelector<T>
    ├── SettingsScreen.kt    # Options screen
    ├── StudyScreen.kt       # 18×18 scrollable type chart grid
    ├── QuizScreen.kt        # Quiz scaffold
    ├── components/
    │   └── TypeBadge.kt     # Reusable type color badge
    ├── navigation/
    │   └── Routes.kt        # Type-safe serializable route objects
    ├── stats/
    │   ├── StatsViewModel.kt
    │   └── StatsScreen.kt
    ├── quiz/
    │   ├── QuizViewModel.kt # AndroidViewModel; owns QuizState via StateFlow
    │   ├── QuizContent.kt   # Active question card; Classic or Pokémon rendering
    │   └── ResultsScreen.kt # Score, dot summary, history
    └── theme/
        ├── Colors.kt
        └── Theme.kt         # Light + dark color schemes
```

### Key types

- **`PokemonType`** — enum of all 18 types, each with `displayName` and `color`
- **`Generation`** — `GEN1`, `GEN2_5`, `GEN6_PLUS`; drives which chart is used
- **`QuizMode`** — `SINGLE` or `DOUBLE`
- **`QuizLength`** — `FIVE`, `TEN`, `TWENTY`, or `ENDLESS` (count is `null` for endless)
- **`PresentationMode`** — `CLASSIC` or `POKEMON`
- **`QuizQuestion`** — types, correct answer, and optional `PokemonEntry` fields for Pokémon mode
- **`AppSettings`** — all five settings collected into one data class from `SettingsRepository`
- **`PokemonEntry`** — `(id, name)` with a computed `spriteUrl` property

### Key functions

- `getEffectiveness(attacking, defending, gen): Float` — core chart lookup (`TypeChart.kt`)
- `availableTypes(gen)` / `filteredTypes(gen, difficulty)` — type pool for a given gen + difficulty
- `multiplierLabel(mult)` — float → `"½×"` or `"4×"` display string
- `generateQuestion(gen, mode, difficulty)` — picks random types and computes multiplier
- `randomPokemonForType(type)` — returns a random `PokemonEntry` for the given type
- `SegmentedSelector<T>()` — generic segmented control used throughout settings and home

---

## Tech Stack

| Component | Library / Version |
|-----------|------------------|
| Language | Kotlin 2.2.10 |
| UI | Jetpack Compose (BOM 2024.10.01) |
| Material Design | Material3 |
| Navigation | Navigation Compose 2.8.4 (type-safe) |
| Persistence | DataStore Preferences 1.1.1 |
| Database | Room 2.7.1 (KSP 2.3.2) |
| Image loading | Coil 3.1.0 (OkHttp network fetcher) |
| Min SDK | 26 (Android 8.0) |
| Target / Compile SDK | 37 |

---

## Build

```bash
./gradlew assembleDebug
```

Sprite images are fetched from the PokéAPI GitHub sprites repository on first launch and cached to `filesDir/pokemon_sprites` (50 MB cap). Subsequent runs work fully offline.

All type chart data and Pokémon mappings are hardcoded in the app.
