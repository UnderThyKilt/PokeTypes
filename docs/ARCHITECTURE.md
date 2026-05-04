# PokeTypes — Architecture Document

## 1. Current Architecture

### Technology Stack

| Component | Library / Version |
|-----------|------------------|
| Language | Kotlin 2.0.21 |
| UI | Jetpack Compose (BOM 2024.10.01) |
| Material Design | Material3 |
| Navigation | Navigation Compose 2.8.4 (type-safe, `@Serializable`) |
| Serialization | kotlinx.serialization 1.7.3 |
| Persistence | DataStore Preferences 1.1.1 |
| Database | Room 2.7.1 (KSP 2.0.21-1.0.28) |
| ViewModel | lifecycle-viewmodel-compose 2.8.7 |
| Min SDK | 26 (Android 8.0) |
| Target / Compile SDK | 37 (Android 15) |
| Java target | 11 |
| Theming | Light + dark mode (user-toggleable, persisted in DataStore) |

### Package Structure

```
com.underthykilt.poketypes/
├── MainActivity.kt              # Single activity, NavHost, edge-to-edge setup
├── data/
│   ├── TypeChart.kt             # PokemonType, Generation enums; chart logic
│   ├── QuizLogic.kt             # generateQuestion(), scoring helpers
│   ├── Difficulty.kt            # Difficulty enum (NORMAL/HARD) + filteredTypes()
│   ├── ThemeRepository.kt       # Dark/light preference persisted in DataStore
│   ├── ScoreRepository.kt       # ScoreRepository interface + DataStoreScoreRepository
│   ├── TypeStatEntity.kt        # Room entity: one row per answered question
│   ├── TypeStatDao.kt           # Room DAO + TypeAccuracyRow projection
│   ├── TypeStatDatabase.kt      # RoomDatabase singleton
│   └── TypeStatRepository.kt    # TypeStatRepository interface + RoomTypeStatRepository
└── ui/
    ├── HomeScreen.kt            # Generation selector, mode buttons, score history
    ├── StudyScreen.kt           # 18×18 scrollable type chart grid
    ├── components/
    │   └── TypeBadge.kt         # Reusable type badge shared across screens
    ├── navigation/
    │   └── Routes.kt            # Type-safe serializable route objects
    ├── stats/
    │   ├── StatsViewModel.kt    # Combines attacking/defending accuracy flows
    │   └── StatsScreen.kt       # Per-type accuracy bars, attacking/defending tabs
    ├── quiz/
    │   ├── QuizViewModel.kt     # AndroidViewModel; owns QuizState via StateFlow
    │   ├── QuizContent.kt       # Active question card, answer buttons, progress
    │   └── ResultsScreen.kt     # Score card, wrong-answer review, history row
    └── theme/
        ├── Colors.kt            # Effectiveness colors, quiz/study palette
        └── Theme.kt             # Dark color scheme, PokeTypesTheme
```

### Data Flow

```
MainActivity
  └── NavHost (type-safe routes)
        ├── HomeRoute   → HomeScreen
        ├── StudyRoute  → StudyScreen (receives generationName)
        └── QuizRoute   → QuizScreen  (receives generationName, quizModeName)
                              └── QuizViewModel (StateFlow<QuizState>)
                                    └── ScoreRepository → DataStore
```

State lives in two places:

- **ViewModel** — `QuizState` (all quiz state) exposed as a single `StateFlow` from `QuizViewModel`
- **Route arguments** — `generation` and `quizMode` are passed as typed navigation arguments; no activity-level mutable state

### `QuizState` Data Class

```kotlin
data class QuizState(
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

### What Works Well

- **Correct domain models** — `PokemonType`, `Generation`, and `QuizQuestion` are clear and well-named
- **Accurate type chart** — all 143 matchup entries across three generations are hardcoded and correct, including Gen 1 quirks (Ghost→Psychic = 0×)
- **MVVM structure** — `QuizViewModel` owns all quiz state; composables are stateless receivers
- **Type-safe navigation** — broken routes are a compile error, not a runtime crash
- **Repository abstraction** — `ScoreRepository` and `ThemeRepository` decouple storage from UI; DataStore backends are async-safe
- **Light/dark mode** — user-toggleable theme persisted in DataStore; all colors use `MaterialTheme.colorScheme` tokens
- **Difficulty setting** — Normal (10 core types) and Hard (all generation types); threaded through routes and ViewModel
- **Per-type statistics** — every answered question recorded in Room; Stats screen shows attacking/defending accuracy per type, sorted worst-first
- **Reusable UI components** — `SegmentedSelector<T>` and `TypeBadge` are generic and composable
- **Lean dependency footprint** — only Compose, Navigation, DataStore, Lifecycle, and Material3
- **Unit-tested logic** — `TypeChartTest`, `QuizLogicTest`, and `ScoreHistoryTest` cover core domain
- **Offline-first** — no network requirements, works anywhere

---

## 2. Known Limitations

| Issue | Location | Impact |
|-------|----------|--------|
| UI strings partially hardcoded | Most `ui/` files | Some labels still inline Kotlin strings; localization not yet complete |
| No `contentDescription` on type badges | `TypeBadge.kt` | Screen readers cannot identify type names in quiz and study views |
| No instrumented (UI) tests | — | Compose rendering and navigation flows are not covered by automation |

---

## 3. Future Improvements

### Near-term

#### String resources

Move remaining hardcoded UI strings to `res/values/strings.xml`. Labels like `"Master the type chart"`, `"Single Type Quiz"`, `"How effective is"`, and all performance messages (`"Perfect!"`, `"Great job!"`, etc.) should become string resources. This is required for full localization support.

#### Accessibility

Add `contentDescription` to all `TypeBadge` composables. Add a color-meaning legend to `StudyScreen` for users who cannot distinguish effectiveness colors.

---

### Tier 4 — Future Features

These are ideas, not commitments. Each requires additional libraries or significant new work.

| Feature | Dependencies needed | Notes |
|---------|-------------------|-------|
| **PokéAPI integration** | Retrofit, OkHttp, Coroutines | Show Pokémon sprites on quiz questions; requires network permission |

---

## 4. Recommended Reading Order

For anyone new to the codebase:

1. `data/TypeChart.kt` — start here; all domain models and the type chart logic
2. `data/QuizLogic.kt` — question generation and scoring helpers
3. `ui/navigation/Routes.kt` — understand the three serializable route types
4. `MainActivity.kt` — NavHost wiring; see how routes map to screens
5. `ui/HomeScreen.kt` — simplest screen; shows the `SegmentedSelector` reuse pattern
6. `ui/StudyScreen.kt` — chart rendering; see how `getEffectiveness()` drives the grid
7. `ui/quiz/QuizViewModel.kt` — state management; read `QuizState` first, then the event handlers
8. `ui/quiz/QuizContent.kt` and `ui/quiz/ResultsScreen.kt` — UI consumers of `QuizState`
