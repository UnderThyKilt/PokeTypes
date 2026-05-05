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
├── PokeTypesApp.kt              # Application subclass; SingletonImageLoader.Factory for Coil;
│                                #   pokemonRepository: PokemonRepository lazy singleton
├── MainActivity.kt              # Single activity, NavHost, edge-to-edge setup
├── data/
│   ├── TypeChart.kt             # PokemonType, Generation enums; chart logic
│   ├── QuizLogic.kt             # QuizQuestion (+ reverse-mode fields), QuizMode (all four),
│   │                            #   generateQuestion(), SINGLE_CHOICES, DOUBLE_CHOICES, questionKey()
│   ├── Difficulty.kt            # Difficulty enum (NORMAL/HARD) + filteredTypes()
│   ├── QuizLength.kt            # QuizLength enum (FIVE/TEN/TWENTY/ENDLESS; count: Int?)
│   ├── PresentationMode.kt      # PresentationMode enum (CLASSIC/POKEMON)
│   ├── SpriteGeneration.kt      # SpriteGeneration enum (GEN1–GEN8/ALL); holds maxGen: Int
│   ├── SettingsRepository.kt    # AppSettings data class + DataStore-backed repository
│   ├── ScoreRepository.kt       # ScoreRepository interface + DataStoreScoreRepository; QUIZ_LENGTH
│   ├── TypeStatEntity.kt        # Room entity: one row per answered question
│   ├── TypeStatDao.kt           # Room DAO + TypeAccuracyRow projection
│   ├── TypeStatDatabase.kt      # RoomDatabase singleton
│   ├── TypeStatRepository.kt    # TypeStatRepository interface + RoomTypeStatRepository
│   └── pokemon/
│       ├── PokemonEntry.kt      # data class(id, name, generation) + spriteUrl computed property
│       ├── PokemonRepository.kt # PokemonRepository interface + AssetPokemonRepository
│       │                        #   (lazy-parses pokemon.json; Map<Set<PokemonType>, List<PokemonEntry>>)
│       └── TypePokemon.kt       # Pointer only — data migrated to app/src/main/assets/pokemon.json
├── domain/
│   ├── EnrichWithPokemonUseCase.kt      # Attaches PokemonEntry sprites to a QuizQuestion;
│   │                                    #   reverse mode only enriches the attacker
│   └── GenerateQuizQuestionsUseCase.kt  # Builds deduplicated question lists for fixed/endless quizzes
└── ui/
    ├── HomeScreen.kt            # Mode buttons split into Classic/Reverse sections; SegmentedSelector<T>
    ├── SettingsScreen.kt        # Options screen: theme, generation, difficulty, length, style, sprite gen
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
    │   ├── QuizViewModel.kt     # AndroidViewModel; owns QuizState via StateFlow;
    │   │                        #   delegates to GenerateQuizQuestionsUseCase
    │   ├── QuizContent.kt       # Active question card; Classic, Pokémon, and Reverse rendering
    │   └── ResultsScreen.kt     # Score card, wrong-answer review, history row
    └── theme/
        ├── Colors.kt            # Effectiveness colors, quiz/study palette
        └── Theme.kt             # Light + dark color schemes, PokeTypesTheme
```

### Data Flow

```
PokeTypesApp (Application)
  ├── Coil ImageLoader — persistent disk cache at filesDir/pokemon_sprites
  └── pokemonRepository: AssetPokemonRepository — lazy-parsed from assets/pokemon.json

MainActivity
  ├── SettingsRepository (DataStore) → AppSettings (StateFlow)
  └── NavHost (type-safe routes)
        ├── HomeRoute    → HomeScreen
        ├── SettingsRoute → SettingsScreen  (reads/writes AppSettings via repository)
        ├── StatsRoute   → StatsScreen      (StatsViewModel ← Room TypeStatDatabase)
        ├── StudyRoute   → StudyScreen      (receives generationName)
        └── QuizRoute    → QuizScreen       (gen, mode, difficulty, length, presentationMode, spriteGeneration)
                                └── QuizViewModel (StateFlow<QuizState>)
                                      ├── GenerateQuizQuestionsUseCase
                                      │     └── EnrichWithPokemonUseCase
                                      │           └── PokemonRepository (AssetPokemonRepository)
                                      ├── ScoreRepository (DataStore) — fixed-length score history
                                      └── TypeStatRepository (Room) — per-question recording
```

State lives in two places:

- **ViewModel** — `QuizState` (all quiz state) exposed as a single `StateFlow` from `QuizViewModel`
- **Route arguments** — generation, quizMode, difficulty, quizLength, presentationMode, and spriteGeneration are typed navigation arguments; no mutable activity state

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
    val isDarkTheme: Boolean                = true,
    val generation: Generation              = Generation.GEN6_PLUS,
    val difficulty: Difficulty              = Difficulty.HARD,
    val quizLength: QuizLength              = QuizLength.TEN,
    val presentationMode: PresentationMode  = PresentationMode.CLASSIC,
    val spriteGeneration: SpriteGeneration  = SpriteGeneration.ALL,
)
```

All six fields are persisted as individual string/boolean keys in a single DataStore file (`"settings"`). Each is read with `runCatching { Enum.valueOf(it) }` so stale stored values fail gracefully to the default.

### Quiz Modes

Four modes are supported via `QuizMode`:

| Mode | `answerChoices` | `correctAnswer` | `promptMultiplier` |
|------|-----------------|-----------------|--------------------|
| `SINGLE` | empty | effectiveness float | null |
| `DOUBLE` | empty | effectiveness float | null |
| `REVERSE_SINGLE` | 4 `Pair<PokemonType, null>` | index of correct pair | multiplier shown as question |
| `REVERSE_DOUBLE` | 4 `Pair<PokemonType, PokemonType>` | index of correct pair | multiplier shown as question |

`QuizContent` detects forward vs. reverse by checking `q.answerChoices.isNotEmpty()`. In reverse mode the question card shows the attacking type and the target multiplier; answer buttons display `TypeBadge` rows. In forward mode answer buttons display multiplier text.

### Pokémon Mode

When `presentationMode == POKEMON`, each `QuizQuestion` is enriched with `PokemonEntry` values before being added to the quiz. Enrichment and filtering are handled by two use cases in the `domain/` package:

**`EnrichWithPokemonUseCase`**
- `enrich(q, mode, spriteGen)` — copies the question with `attackingPokemon` (and `defendingPokemon` for forward modes) filled from `PokemonRepository`
- `hasRequiredPokemon(q, mode, spriteGen)` — deterministic check used to discard questions before enrichment; in reverse mode only the attacker is required

**`GenerateQuizQuestionsUseCase`**
- `buildInitialQuestions(count, ...)` — generates a deduplicated list of `count` questions for fixed-length quizzes; tries up to `count × 50` candidates before falling back to allow duplicates
- `generateNext(...)` — generates one question for endless mode; called from `QuizViewModel.doAdvance()`

`QuizContent` renders a `PokemonCard` (sprite via `SubcomposeAsyncImage` + Pokédex number + name + small `TypeBadge`) or falls back to the classic large `TypeBadge`. Sprite load failures show a circular "?" placeholder.

Sprites are fetched from `https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/{id}.png`. Coil's `OkHttpNetworkFetcherFactory` handles the network; a `DiskCache` in `filesDir/pokemon_sprites` (50 MB cap) provides offline availability after first use.

### Pokémon Data Asset

All Pokémon data lives in `app/src/main/assets/pokemon.json` — a flat JSON array generated by `test/generate_pokemon_data.py`. Do not edit by hand.

```json
[{"id": 1, "name": "Bulbasaur", "generation": 1, "types": ["GRASS", "POISON"]}, ...]
```

1025 base-form Pokémon across 154 type combinations. `AssetPokemonRepository` lazy-parses this file once using `kotlinx.serialization` and builds a `Map<Set<PokemonType>, List<PokemonEntry>>` identical in structure to the old compiled Kotlin map. `spriteUrl` is a computed property on `PokemonEntry` and is not stored in the JSON.

To regenerate after a new Pokémon generation ships:

```bash
python test/generate_pokemon_data.py
```

To verify all sprite URLs are reachable:

```bash
python test/check_sprites.py
```

### What Works Well

- **Correct domain models** — `PokemonType`, `Generation`, and `QuizQuestion` are clear and well-named
- **Accurate type chart** — all 143 matchup entries across three generations are hardcoded and correct, including Gen 1 quirks (Ghost→Psychic = 0×)
- **Use-case layer** — `EnrichWithPokemonUseCase` and `GenerateQuizQuestionsUseCase` in `domain/` are pure Kotlin, fully testable without Android
- **MVVM structure** — `QuizViewModel` owns all quiz state; composables are stateless receivers
- **Type-safe navigation** — broken routes are a compile error, not a runtime crash
- **Repository abstraction** — `SettingsRepository`, `ScoreRepository`, `TypeStatRepository`, and `PokemonRepository` decouple storage from UI and are mockable in tests
- **Light/dark mode** — user-toggleable theme persisted in DataStore; all colors use `MaterialTheme.colorScheme` tokens
- **Difficulty setting** — Normal (10 core types) and Hard (all generation types); threaded through routes and ViewModel
- **Quiz length** — 5, 10, 20 questions or Endless; endless mode generates one question at a time and doesn't save score
- **All four quiz modes** — forward single, forward dual, reverse single, and reverse dual; reverse modes test inverse knowledge (given a multiplier, pick the type)
- **No-duplicate questions** — `seenKeys: MutableSet<Any>` with `questionKey()` normalizing dual-type order prevents repeats
- **Per-type statistics** — every answered question recorded in Room; Stats screen shows attacking/defending accuracy per type, sorted worst-first
- **Pokémon sprites** — 1025 base-form Pokémon with generation-aware filtering; questions without a valid sprite are skipped automatically; asset-based, no compile-time overhead
- **Reusable UI components** — `SegmentedSelector<T>` and `TypeBadge` are generic and composable
- **Tested logic** — unit tests cover type chart, forward/reverse quiz generation, score history, and use-case behavior; custom Gradle tasks for running individual test classes

---

## 2. Known Limitations

| Issue | Location | Impact |
|-------|----------|--------|
| UI strings partially hardcoded | Most `ui/` files | Some labels still inline Kotlin strings; localization not yet complete |
| No `contentDescription` on type badges | `TypeBadge.kt` | Screen readers cannot identify type names in quiz and study views |

---

## 3. Future Improvements

### Near-term

#### String resources

Move remaining hardcoded UI strings to `res/values/strings.xml`. Labels like `"Master the type chart"`, `"Single Type Quiz"`, `"How effective is"`, and all performance messages (`"Perfect!"`, `"Great job!"`, etc.) should become string resources. This is required for full localization support.

#### Accessibility

Add `contentDescription` to all `TypeBadge` composables. Add a color-meaning legend to `StudyScreen` for users who cannot distinguish effectiveness colors.

---

## 4. Testing

### Test files

| File | Package | Type | What it covers |
|------|---------|------|----------------|
| `TypeChartTest` | `data` | Unit | `getEffectiveness()` across all three generations |
| `QuizLogicTest` | root | Unit | Forward/reverse question generation, answer choice correctness, `questionKey` order independence |
| `ScoreHistoryTest` | root | Unit | Score CSV parsing, appending, max-size capping |
| `EnrichWithPokemonUseCaseTest` | `domain` | Unit | Classic no-op, Pokémon enrichment for forward/reverse, `hasRequiredPokemon` logic |
| `GenerateQuizQuestionsUseCaseTest` | `domain` | Unit | Question count for all modes, `seenKeys` deduplication, generation and difficulty filtering |
| `SpriteUrlTest` | `data.pokemon` | On-demand | HEAD requests to verify all `pokemon.json` entries have reachable sprites |

All unit tests use inline anonymous fakes as test doubles — no mocking framework required for the current suite.

### Running tests

```
Gradle panel → app → Tasks → verification
  testQuizLogic                     — runs QuizLogicTest only
  testEnrichWithPokemonUseCase       — runs EnrichWithPokemonUseCaseTest only
  testGenerateQuizQuestionsUseCase   — runs GenerateQuizQuestionsUseCaseTest only
  testDebugUnitTest                  — runs the full unit test suite
```

Or use the green ▶ gutter icon next to any `@Test` in the editor.

### Test dependencies

| Library | Version | Scope | Purpose |
|---------|---------|-------|---------|
| JUnit 4 | 4.13.2 | `testImplementation` | Core runner |
| kotlinx-coroutines-test | 1.8.1 | `testImplementation` | `runTest`, `TestScope` for suspend / Flow |
| Turbine | 1.2.0 | `testImplementation` | `Flow`/`StateFlow` assertion DSL |
| MockK | 1.13.12 | `testImplementation` | Kotlin-native mocking |
| Robolectric | 4.13 | `testImplementation` | Android framework on JVM (Context, assets) |
| room-testing | 2.7.1 | `testImplementation` | In-memory Room DB for DAO tests |
| androidx-test-ext-junit | 1.2.1 | `androidTestImplementation` | `AndroidJUnit4` runner for instrumented tests |
| espresso-core | 3.6.1 | `androidTestImplementation` | Device/emulator UI interaction tests |
| Compose UI test | BOM | `androidTestImplementation` | `composeTestRule.onNodeWithText(...)` assertions |

`testOptions { unitTests { isIncludeAndroidResources = true } }` is set in `app/build.gradle.kts` so that Robolectric tests can access `assets/` and `res/` files.

---

## 5. Recommended Reading Order

For anyone new to the codebase:

1. `data/TypeChart.kt` — start here; all domain models and the type chart logic
2. `data/QuizLogic.kt` — question generation for all four modes; reverse encoding with `answerChoices` / `promptMultiplier`
3. `data/SettingsRepository.kt` — see how all six settings are consolidated into one DataStore file
4. `data/pokemon/PokemonRepository.kt` — understand the `AssetPokemonRepository` and how `pokemon.json` is parsed
5. `domain/EnrichWithPokemonUseCase.kt` and `domain/GenerateQuizQuestionsUseCase.kt` — use-case layer
6. `ui/navigation/Routes.kt` — understand the five serializable route types
7. `MainActivity.kt` — NavHost wiring; see how routes map to screens
8. `ui/HomeScreen.kt` — simplest screen; shows the `SegmentedSelector` reuse pattern
9. `ui/StudyScreen.kt` — chart rendering; see how `getEffectiveness()` drives the grid
10. `ui/quiz/QuizViewModel.kt` — state management; read `QuizState` first, then the event handlers
11. `ui/quiz/QuizContent.kt` — UI consumer of `QuizState`; see Classic vs Pokémon vs Reverse mode branching
12. `PokeTypesApp.kt` — Coil singleton setup, disk cache configuration, and `pokemonRepository` lazy init
13. `test/generate_pokemon_data.py` — understand how `pokemon.json` is generated from PokéAPI
