# PokeTypes

An Android app for learning Pokémon type matchups. Study the full type chart, test your knowledge with single, dual-type, and reverse quizzes across three game generations, and track your per-type accuracy over time.

---

## Screens

### Home
Entry point. Shows the current settings summary and launches any mode. Classic quiz modes and Reverse quiz modes are shown in separate sections.

### Options
Persistent settings screen:
- **Dark Mode** toggle
- **Generation** — Gen 1 / Gen 2–5 / Gen 6+
- **Difficulty** — Normal (10 core types) / Hard (all types for the selected generation)
- **Quiz Length** — 5, 10, 20, or Endless
- **Style** — Classic (type badges) or Pokémon (sprites + name + badge)
- **Pokémon Generation** — Gen 1 through Gen 8, or All (only shown when Style is Pokémon)

### Study Chart
An 18×18 scrollable grid of type matchups. Tap any attacking type to highlight its row. Cells are color-coded:
- Green — super effective (2×)
- Red — not very effective (½×)
- Dark — no effect (0×)
- Neutral — normal (1×)

### Quiz
Four modes:

| Mode | Question | Answer choices |
|------|----------|----------------|
| Single Type | How effective is `[type]` vs `[type]`? | 0×, ½×, 1×, 2× |
| Dual Type | How effective is `[type]` vs `[type]/[type]`? | 0×, ¼×, ½×, 1×, 2×, 4× |
| Reverse Single | Which type receives `[mult]` from `[type]`? | 4 type choices |
| Reverse Dual | Which type pair receives `[mult]` from `[type]`? | 4 type-pair choices |

Questions are generated without duplicates within a session. Correct answers turn green, wrong answers turn red. If a wrong answer is selected, feedback shows the actual effectiveness of the chosen type. A streak counter appears on consecutive correct answers.

In **Classic** style, questions show colored type badges. In **Pokémon** style, questions show a Pokémon sprite, Pokédex number, name, and small type badge instead. Sprites are downloaded on first use and cached persistently — no internet required after that.

When a **Pokémon Generation** is set, only Pokémon from that generation or earlier are shown as sprites, and questions where no matching Pokémon exists for the generation are automatically skipped.

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
├── PokeTypesApp.kt          # Application subclass; Coil persistent sprite cache; pokemonRepository singleton
├── MainActivity.kt          # Single activity, NavHost, settings state
├── data/
│   ├── TypeChart.kt         # PokemonType enum, Generation enum, type chart logic
│   ├── QuizLogic.kt         # QuizQuestion, QuizMode (incl. REVERSE_SINGLE/DOUBLE), generateQuestion()
│   ├── Difficulty.kt        # Difficulty enum + filteredTypes()
│   ├── QuizLength.kt        # QuizLength enum (FIVE / TEN / TWENTY / ENDLESS)
│   ├── PresentationMode.kt  # PresentationMode enum (CLASSIC / POKEMON)
│   ├── SpriteGeneration.kt  # SpriteGeneration enum (GEN1–GEN8, ALL) with maxGen: Int
│   ├── SettingsRepository.kt # All user settings persisted in DataStore
│   ├── ScoreRepository.kt   # ScoreRepository interface + DataStoreScoreRepository
│   ├── TypeStatEntity.kt    # Room entity: one row per answered question
│   ├── TypeStatDao.kt       # Room DAO + TypeAccuracyRow projection
│   ├── TypeStatDatabase.kt  # RoomDatabase singleton
│   ├── TypeStatRepository.kt# TypeStatRepository interface + RoomTypeStatRepository
│   └── pokemon/
│       ├── PokemonEntry.kt      # PokemonEntry(id, name, generation) + spriteUrl property
│       ├── PokemonRepository.kt # PokemonRepository interface + AssetPokemonRepository (reads pokemon.json)
│       └── TypePokemon.kt       # Pointer only — data lives in app/src/main/assets/pokemon.json
├── domain/
│   ├── EnrichWithPokemonUseCase.kt      # Attaches PokemonEntry sprites to a QuizQuestion
│   └── GenerateQuizQuestionsUseCase.kt  # Builds deduplicated question lists; drives endless mode
└── ui/
    ├── HomeScreen.kt        # Mode buttons (Classic + Reverse sections), settings summary
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
    │   ├── QuizViewModel.kt # AndroidViewModel; owns QuizState; delegates to use cases
    │   ├── QuizContent.kt   # Active question card; Classic/Pokémon/Reverse rendering
    │   └── ResultsScreen.kt # Score, dot summary, wrong-answer review, history
    └── theme/
        ├── Colors.kt
        └── Theme.kt         # Light + dark color schemes

app/src/main/assets/
└── pokemon.json             # 1025 base-form Pokémon — [{id, name, generation, types}]
                             # Generated by test/generate_pokemon_data.py. Do NOT edit by hand.

test/
├── check_sprites.py         # Verify all entries in pokemon.json have reachable sprite URLs
└── generate_pokemon_data.py # Fetch all Pokémon from PokéAPI and write pokemon.json
```

### Key types

- **`PokemonType`** — enum of all 18 types, each with `displayName` and `color`
- **`Generation`** — `GEN1`, `GEN2_5`, `GEN6_PLUS`; drives which chart is used
- **`QuizMode`** — `SINGLE`, `DOUBLE`, `REVERSE_SINGLE`, `REVERSE_DOUBLE`
- **`QuizLength`** — `FIVE`, `TEN`, `TWENTY`, or `ENDLESS` (count is `null` for endless)
- **`PresentationMode`** — `CLASSIC` or `POKEMON`
- **`SpriteGeneration`** — `GEN1`–`GEN8` or `ALL`; filters Pokémon sprite pool by max generation
- **`QuizQuestion`** — types, correct answer, optional `PokemonEntry` fields, and `answerChoices` / `promptMultiplier` for reverse modes
- **`AppSettings`** — all six settings collected into one data class from `SettingsRepository`
- **`PokemonEntry`** — `(id, name, generation)` with a computed `spriteUrl` property
- **`PokemonRepository`** — interface for type-keyed Pokémon lookups; backed by `AssetPokemonRepository`

### Key functions

- `getEffectiveness(attacking, defending, gen): Float` — core chart lookup (`TypeChart.kt`)
- `availableTypes(gen)` / `filteredTypes(gen, difficulty)` — type pool for a given gen + difficulty
- `multiplierLabel(mult)` — float → `"½×"` or `"4×"` display string
- `generateQuestion(gen, mode, difficulty)` — picks random types and computes multiplier (all four modes)
- `PokemonRepository.randomForTypes(vararg types, maxGeneration)` — random `PokemonEntry` for exact type combo within gen cap
- `PokemonRepository.hasForTypes(vararg types, maxGeneration)` — deterministic existence check used to filter invalid questions
- `SegmentedSelector<T>()` — generic segmented control used throughout settings and home

---

## Tech Stack

| Component | Library / Version |
|-----------|------------------|
| Language | Kotlin 2.2.10 |
| UI | Jetpack Compose (BOM 2024.10.01) |
| Material Design | Material3 |
| Navigation | Navigation Compose 2.8.4 (type-safe) |
| Serialization | kotlinx.serialization 1.7.3 |
| Persistence | DataStore Preferences 1.1.1 |
| Database | Room 2.7.1 (KSP 2.3.2) |
| Image loading | Coil 3.1.0 (OkHttp network fetcher) |
| Min SDK | 26 (Android 8.0) |
| Target / Compile SDK | 37 |

---

## Build

Open in Android Studio and run, or build via Gradle.

Sprite images are fetched from the PokéAPI GitHub sprites repository on first launch and cached to `filesDir/pokemon_sprites` (50 MB cap). Subsequent runs work fully offline.

### Regenerating Pokémon data

`pokemon.json` is auto-generated from PokéAPI. To refresh it (e.g. after a new generation is released):

```bash
python test/generate_pokemon_data.py
```

Writes `app/src/main/assets/pokemon.json` as a flat JSON array of `{id, name, generation, types}` objects. API responses are cached to `test/.pokeapi_cache/` so re-runs are instant without network access.

### Verifying sprite URLs

```bash
python test/check_sprites.py
```

Reads every entry in `pokemon.json` and checks its sprite URL with a HEAD request, reporting any non-200 responses.

---

## Testing

Unit tests live in `app/src/test/` and run on the JVM (no device needed).

| Test file | What it covers |
|-----------|---------------|
| `TypeChartTest` | Type effectiveness values across all three generations |
| `QuizLogicTest` | Forward and reverse question generation, answer choice correctness, `questionKey` deduplication |
| `ScoreHistoryTest` | Score parsing, appending, and history capping |
| `EnrichWithPokemonUseCaseTest` | Classic no-op, Pokémon enrichment for forward/reverse modes, `hasRequiredPokemon` |
| `GenerateQuizQuestionsUseCaseTest` | Question count, `seenKeys` deduplication, generation filtering, difficulty filtering |
| `SpriteUrlTest` | All entries in `pokemon.json` have reachable HTTP 200 sprite URLs (run on demand, requires internet) |

### Running tests

- **Single class** — click the green ▶ gutter icon next to the class or any `@Test` method in the editor
- **Single file** — right-click the file in the Project panel → Run
- **Named Gradle tasks** (Gradle panel → app → Tasks → verification):
  - `testQuizLogic`
  - `testEnrichWithPokemonUseCase`
  - `testGenerateQuizQuestionsUseCase`
- **All unit tests** — `testDebugUnitTest`

### Test dependencies

| Library | Version | Purpose |
|---------|---------|---------|
| JUnit 4 | 4.13.2 | Core test runner |
| kotlinx-coroutines-test | 1.8.1 | `runTest`, `TestScope` for suspend functions and `StateFlow` |
| Turbine | 1.2.0 | Flow/StateFlow assertion DSL |
| MockK | 1.13.12 | Kotlin-native mocking |
| Robolectric | 4.13 | Run Android framework code (Context, assets) on JVM |
| room-testing | 2.7.1 | In-memory Room database for DAO tests |
| androidx-test-ext-junit | 1.2.1 | `@RunWith(AndroidJUnit4::class)` for instrumented tests |
| espresso-core | 3.6.1 | UI interaction tests on device/emulator |
| Compose UI test | (BOM) | `composeTestRule.onNodeWithText(...)` Compose assertions |
