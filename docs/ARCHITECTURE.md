# PokeTypes — Architecture Document

## 1. Current Architecture

### Technology Stack

| Component | Library / Version |
|-----------|------------------|
| Language | Kotlin 2.2.10 |
| UI | Jetpack Compose (BOM 2024.10.01) |
| Material Design | Material3 |
| Navigation | Navigation Compose 2.8.4 (type-safe, `@Serializable`) |
| Serialization | kotlinx.serialization 1.7.3 |
| Persistence | DataStore Preferences 1.1.1 |
| Database | Room 2.7.1 (KSP 2.3.2) |
| Image loading | Coil 3.1.0 (`coil-compose` + `coil-network-okhttp`) |
| ViewModel | lifecycle-viewmodel-compose 2.8.7 |
| Min SDK | 26 (Android 8.0) |
| Target / Compile SDK | 37 |
| Java target | 11 |

### Package Structure

```
com.underthykilt.poketypes/
├── PokeTypesApp.kt              # Application subclass; SingletonImageLoader.Factory for Coil
├── MainActivity.kt              # Single activity, NavHost, edge-to-edge setup
├── data/
│   ├── TypeChart.kt             # PokemonType, Generation enums; chart logic
│   ├── QuizLogic.kt             # QuizQuestion (+ optional PokemonEntry fields), generateQuestion()
│   ├── Difficulty.kt            # Difficulty enum (NORMAL/HARD) + filteredTypes()
│   ├── QuizLength.kt            # QuizLength enum (FIVE/TEN/TWENTY/ENDLESS; count: Int?)
│   ├── PresentationMode.kt      # PresentationMode enum (CLASSIC/POKEMON)
│   ├── SettingsRepository.kt    # AppSettings data class + DataStore-backed repository
│   ├── ScoreRepository.kt       # ScoreRepository interface + DataStoreScoreRepository
│   ├── TypeStatEntity.kt        # Room entity: one row per answered question
│   ├── TypeStatDao.kt           # Room DAO + TypeAccuracyRow projection
│   ├── TypeStatDatabase.kt      # RoomDatabase singleton
│   ├── TypeStatRepository.kt    # TypeStatRepository interface + RoomTypeStatRepository
│   └── pokemon/
│       ├── PokemonEntry.kt      # data class(id, name) + spriteUrl computed property
│       └── TypePokemon.kt       # Hardcoded ~7 Pokémon per type; randomPokemonForType()
└── ui/
    ├── HomeScreen.kt            # Mode buttons, settings summary, SegmentedSelector<T>
    ├── SettingsScreen.kt        # Options screen: theme, generation, difficulty, length, style
    ├── StudyScreen.kt           # 18×18 scrollable type chart grid
    ├── QuizScreen.kt            # Scaffold + ViewModel wiring for quiz flow
    ├── components/
    │   └── TypeBadge.kt         # Reusable type color badge
    ├── navigation/
    │   └── Routes.kt            # Type-safe serializable route objects
    ├── stats/
    │   ├── StatsViewModel.kt    # Combines attacking/defending accuracy flows
    │   └── StatsScreen.kt       # Per-type accuracy bars, attacking/defending tabs
    ├── quiz/
    │   ├── QuizViewModel.kt     # AndroidViewModel; owns QuizState via StateFlow
    │   ├── QuizContent.kt       # Active question card; Classic or Pokémon mode rendering
    │   └── ResultsScreen.kt     # Score card, wrong-answer review, history row
    └── theme/
        ├── Colors.kt            # Effectiveness colors, quiz/study palette
        └── Theme.kt             # Light + dark color schemes, PokeTypesTheme
```

### Data Flow

```
PokeTypesApp (Application)
  └── Coil ImageLoader — persistent disk cache at filesDir/pokemon_sprites

MainActivity
  ├── SettingsRepository (DataStore) → AppSettings (StateFlow)
  └── NavHost (type-safe routes)
        ├── HomeRoute    → HomeScreen
        ├── SettingsRoute → SettingsScreen  (reads/writes AppSettings via repository)
        ├── StatsRoute   → StatsScreen      (StatsViewModel ← Room TypeStatDatabase)
        ├── StudyRoute   → StudyScreen      (receives generationName)
        └── QuizRoute    → QuizScreen       (receives gen, mode, difficulty, length, presentationMode)
                                └── QuizViewModel (StateFlow<QuizState>)
                                      ├── ScoreRepository (DataStore) — fixed-length score history
                                      ├── TypeStatRepository (Room) — per-question recording
                                      └── randomPokemonForType() — Pokémon enrichment (POKEMON mode only)
```

State lives in two places:

- **ViewModel** — `QuizState` (all quiz state) exposed as a single `StateFlow` from `QuizViewModel`
- **Route arguments** — generation, quizMode, difficulty, quizLength, and presentationMode are typed navigation arguments; no mutable activity state

### `QuizState` Data Class

```kotlin
data class QuizState(
    val quizLength: Int? = 10,           // null = endless
    val questions: List<QuizQuestion>,
    val questionIndex: Int = 0,
    val selected: Float? = null,
    val correctAnswers: Int = 0,
    val streak: Int = 0,
    val questionResults: List<Boolean> = emptyList(),
    val userAnswers: List<Float> = emptyList(),
    val quizComplete: Boolean = false,
    val history: List<Int> = emptyList(),
)
```

### `AppSettings` Data Class

```kotlin
data class AppSettings(
    val isDarkTheme: Boolean            = true,
    val generation: Generation          = Generation.GEN6_PLUS,
    val difficulty: Difficulty          = Difficulty.HARD,
    val quizLength: QuizLength          = QuizLength.TEN,
    val presentationMode: PresentationMode = PresentationMode.CLASSIC,
)
```

All five fields are persisted as individual string/boolean keys in a single DataStore file (`"settings"`). Each is read with `runCatching { Enum.valueOf(it) }` so stale stored values fail gracefully to the default.

### Pokémon Mode

When `presentationMode == POKEMON`, each `QuizQuestion` is enriched with `PokemonEntry` values for the attacking and defending types:

```kotlin
data class QuizQuestion(
    ...
    val attackingPokemon: PokemonEntry? = null,
    val defendingPokemon: PokemonEntry? = null,
    val defendingPokemon2: PokemonEntry? = null,
)
```

Enrichment happens in `QuizViewModel` via a private `QuizQuestion.withPokemon()` extension. `QuizContent` detects mode by checking `q.attackingPokemon != null` and renders either a `PokemonCard` (sprite via `AsyncImage` + name + small `TypeBadge`) or the classic large `TypeBadge`.

Sprites are fetched from `https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/{id}.png`. Coil's `OkHttpNetworkFetcherFactory` handles the network; a `DiskCache` in `filesDir/pokemon_sprites` (50 MB cap) provides offline availability after first use.

### What Works Well

- **Correct domain models** — `PokemonType`, `Generation`, and `QuizQuestion` are clear and well-named
- **Accurate type chart** — all 143 matchup entries across three generations are hardcoded and correct, including Gen 1 quirks (Ghost→Psychic = 0×)
- **MVVM structure** — `QuizViewModel` owns all quiz state; composables are stateless receivers
- **Type-safe navigation** — broken routes are a compile error, not a runtime crash
- **Repository abstraction** — `SettingsRepository`, `ScoreRepository`, and `TypeStatRepository` decouple storage from UI
- **Light/dark mode** — user-toggleable theme persisted in DataStore; all colors use `MaterialTheme.colorScheme` tokens
- **Difficulty setting** — Normal (10 core types) and Hard (all generation types); threaded through routes and ViewModel
- **Quiz length** — 5, 10, 20 questions or Endless; endless mode generates one question at a time and doesn't save score
- **No-duplicate questions** — `seenKeys: MutableSet<Any>` with `questionKey()` normalizing dual-type order prevents repeats
- **Per-type statistics** — every answered question recorded in Room; Stats screen shows attacking/defending accuracy per type, sorted worst-first
- **Pokémon sprites** — optional presentation mode showing sprites cached offline in `filesDir`
- **Reusable UI components** — `SegmentedSelector<T>` and `TypeBadge` are generic and composable
- **Unit-tested logic** — `TypeChartTest`, `QuizLogicTest`, and `ScoreHistoryTest` cover core domain

---

## 2. Known Limitations

| Issue | Location | Impact |
|-------|----------|--------|
| UI strings partially hardcoded | Most `ui/` files | Some labels still inline Kotlin strings; localization not yet complete |
| No `contentDescription` on type badges | `TypeBadge.kt` | Screen readers cannot identify type names in quiz and study views |
| No instrumented (UI) tests | — | Compose rendering and navigation flows are not covered by automation |
| Pokémon list is static | `TypePokemon.kt` | ~7 Pokémon per type; doesn't reflect Gen 1/2/5 regional availability |

---

## 3. Future Improvements

### Near-term

#### String resources

Move remaining hardcoded UI strings to `res/values/strings.xml`. Labels like `"Master the type chart"`, `"Single Type Quiz"`, `"How effective is"`, and all performance messages (`"Perfect!"`, `"Great job!"`, etc.) should become string resources. This is required for full localization support.

#### Accessibility

Add `contentDescription` to all `TypeBadge` composables. Add a color-meaning legend to `StudyScreen` for users who cannot distinguish effectiveness colors.

#### Generation-aware Pokémon pool

Filter `TypePokemon.kt` by generation so only Pokémon introduced by that generation appear in POKEMON mode questions.

---

## 4. Recommended Reading Order

For anyone new to the codebase:

1. `data/TypeChart.kt` — start here; all domain models and the type chart logic
2. `data/QuizLogic.kt` — question generation and scoring helpers
3. `data/SettingsRepository.kt` — see how all five settings are consolidated into one DataStore file
4. `ui/navigation/Routes.kt` — understand the five serializable route types
5. `MainActivity.kt` — NavHost wiring; see how routes map to screens
6. `ui/HomeScreen.kt` — simplest screen; shows the `SegmentedSelector` reuse pattern
7. `ui/StudyScreen.kt` — chart rendering; see how `getEffectiveness()` drives the grid
8. `ui/quiz/QuizViewModel.kt` — state management; read `QuizState` first, then the event handlers
9. `ui/quiz/QuizContent.kt` — UI consumer of `QuizState`; see Classic vs Pokémon mode branching
10. `PokeTypesApp.kt` — Coil singleton setup and disk cache configuration
