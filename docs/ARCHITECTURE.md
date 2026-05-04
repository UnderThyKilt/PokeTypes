# PokeTypes — Architecture Document

## 1. Current Architecture

### Technology Stack

| Component | Library / Version |
|-----------|------------------|
| Language | Kotlin 2.0.21 |
| UI | Jetpack Compose (BOM 2024.10.01) |
| Material Design | Material3 |
| Navigation | Navigation Compose 2.8.4 |
| Persistence | SharedPreferences |
| Min SDK | 26 (Android 8.0) |
| Target / Compile SDK | 37 (Android 14) |

### Package Structure

```
com.underthykilt.poketypes/
├── MainActivity.kt              # Single activity, NavHost, top-level state
├── data/
│   ├── TypeChart.kt             # PokemonType, Generation enums; chart logic
│   └── ScoreHistory.kt          # SharedPreferences persistence singleton
└── ui/
    ├── HomeScreen.kt            # Generation selector, mode buttons
    ├── StudyScreen.kt           # 18×18 scrollable type chart grid
    ├── QuizScreen.kt            # Quiz engine, results screen, TypeBadge
    └── theme/
        └── Theme.kt             # Dark color scheme, PokeTypesTheme
```

### Data Flow

```
MainActivity
  └── NavHost
        ├── "home"  → HomeScreen  (reads: generation, quizMode state)
        ├── "study" → StudyScreen (reads: generation)
        └── "quiz"  → QuizScreen  (reads: generation, quizMode)
                          └── ScoreHistory.save/load → SharedPreferences
```

State lives in two places:
- **Activity level** — `generation` and `quizMode` (`remember { mutableStateOf(...) }` in `MainActivity.kt`)
- **Composable level** — 9 independent state variables inside `QuizScreen` (lines 85–101)

### What Works Well

- **Correct domain models** — `PokemonType`, `Generation`, and `QuizQuestion` are clear and well-named
- **Accurate type chart** — all 143 matchup entries across three generations are hardcoded and correct, including Gen 1 quirks (Ghost→Psychic = 0×)
- **Reusable UI components** — `SegmentedSelector<T>` is generic and clean
- **Lean dependency footprint** — only Compose, Navigation, and Material3; no unnecessary libraries
- **Offline-first** — no network requirements, works anywhere

---

## 2. Known Limitations

The following issues are specific to current source files. Each has a concrete impact on maintainability or correctness.

| Issue | Location | Impact |
|-------|----------|--------|
| No ViewModel — state lives in Activity | `MainActivity.kt` | Quiz progress lost on device rotation |
| 9 loose mutable state vars | `QuizScreen.kt:85–101` | Hard to reason about; impossible to unit test without Compose |
| 551-line monolithic `QuizScreen` | `QuizScreen.kt` | Mixes quiz logic, active question UI, and results rendering in one composable |
| `SharedPreferences` accessed directly in UI | `QuizScreen.kt:85`, `ScoreHistory.kt` | Tight coupling to Android framework; not injectable or mockable |
| Hardcoded colors outside theme | `HomeScreen.kt:73`, `StudyScreen.kt:166–171`, `QuizScreen.kt:41–42` | Theme changes require editing multiple files; colors are inconsistent |
| String-based navigation routes | `MainActivity.kt:32–61` | A typo in `"home"`, `"study"`, or `"quiz"` causes a runtime crash, not a compile error |
| No unit tests | — | Logic regressions (type chart edits, scoring changes) go undetected |
| `isMinifyEnabled = false` in release | `app/build.gradle.kts:20` | Release APK is larger and unobfuscated |
| Java 1.8 target | `app/build.gradle.kts:29–33` | Cuts off modern JVM language features available from Java 11+ |
| All UI strings hardcoded in Kotlin | All `ui/` files | Localization is not possible without rewriting composables |
| No `contentDescription` on type badges | `QuizScreen.kt:532–550` | Screen readers cannot identify type names in quiz and study views |

---

## 3. Proposed Improvements

Improvements are grouped into four tiers. Tier 1 changes unblock everything else; later tiers can be done independently in any order.

---

### Tier 1 — Structural Changes

These address the biggest blockers: rotation resilience and testability. No new libraries required.

#### 1.1 ViewModel per screen

Add `QuizViewModel` to own quiz state and logic. Move `generateQuestion()`, scoring, and streak tracking out of the composable.

```
ui/
├── quiz/
│   ├── QuizViewModel.kt     # new
│   ├── QuizScreen.kt        # trimmed — only UI
│   └── QuizState.kt         # new data class (see 1.2)
```

`lifecycle-viewmodel-compose` is already available transitively through the Compose BOM — no new dependency needed.

#### 1.2 `QuizState` data class

Replace the 9 independent state variables at `QuizScreen.kt:85–101` with a single immutable state holder:

```kotlin
data class QuizState(
    val questions: List<QuizQuestion>,
    val questionIndex: Int = 0,
    val selected: Float? = null,
    val correctAnswers: Int = 0,
    val streak: Int = 0,
    val results: List<Boolean> = emptyList(),
    val userAnswers: List<Float> = emptyList(),
    val quizComplete: Boolean = false,
)
```

The ViewModel exposes a single `StateFlow<QuizState>` and handles all mutations. `resetKey` (the current reset hack) is replaced by re-instantiating the ViewModel or calling a `reset()` function.

#### 1.3 Type-safe navigation

Replace the three string routes in `MainActivity.kt:32–61` with typed objects. Navigation 2.8.4 already supports this via `@Serializable`:

```kotlin
@Serializable object Home
@Serializable data class Study(val generation: String)
@Serializable data class Quiz(val generation: String, val mode: String)
```

This makes broken routes a compile error instead of a runtime crash.

---

### Tier 2 — Clean-up

These improve maintainability. No new libraries needed.

#### 2.1 Split `QuizScreen.kt`

The 551-line file should become three composables:

| File | Responsibility |
|------|---------------|
| `ui/quiz/QuizContent.kt` | Active question card, answer buttons, streak, progress dots |
| `ui/quiz/ResultsScreen.kt` | Score card, wrong answers review, history row, action buttons |
| `ui/components/TypeBadge.kt` | Standalone type badge — shared between quiz and study |

#### 2.2 Consolidate effectiveness colors into the theme

Colors defined in three different files should move to one place:

- `CORRECT_GREEN` (`QuizScreen.kt:41`) → `Theme.kt` or `ui/theme/Colors.kt`
- `WRONG_RED` (`QuizScreen.kt:42`) → same
- `effectivenessColor()` (`StudyScreen.kt:166–171`) → same
- Magic purple `Color(0xFF7038F8)` (`HomeScreen.kt:73`) → add as `quizDual` in the color scheme

#### 2.3 Repository abstraction for score history

Introduce an interface so the storage backend can be swapped without touching the UI:

```kotlin
// data/repository/ScoreRepository.kt
interface ScoreRepository {
    fun save(mode: String, score: Int)
    fun load(mode: String): List<Int>
}

// data/repository/SharedPreferencesScoreRepository.kt
class SharedPreferencesScoreRepository(context: Context) : ScoreRepository { ... }
```

The ViewModel receives a `ScoreRepository`, not a raw `Context`.

#### 2.4 String resources

Move all hardcoded UI strings to `res/values/strings.xml`. Currently only `app_name` is a resource. Labels like `"Master the type chart"`, `"Single Type Quiz"`, `"How effective is"`, and all performance messages (`"Perfect!"`, `"Great job!"`, etc.) are hardcoded Kotlin strings. This is required for localization.

---

### Tier 3 — Infrastructure

These add foundations needed before publishing or growing the team.

#### 3.1 Unit tests

Add a `test/` source set (already scaffolded by the Android project template but unused). Priority test targets:

| Test class | What to cover |
|-----------|--------------|
| `TypeChartTest` | `getEffectiveness()` for known matchups in all three generations; Gen 1 Ghost→Psychic = 0×; Steel resistances in Gen 2–5 vs. Gen 6+ |
| `QuizLogicTest` | `generateQuestion()` never returns same type for both defenders in DOUBLE mode; `scoreColor()` and `performanceMessage()` boundary values |
| `ScoreHistoryTest` | Cap at 10 scores; empty state; malformed comma-separated string handling |

#### 3.2 DataStore

Replace `SharedPreferences` in `ScoreHistory.kt` with `androidx.datastore:datastore-preferences`. DataStore is the current Android recommendation: it is async-safe (returns `Flow`), type-safe, and does not silently drop writes the way `apply()` can.

The `ScoreRepository` interface from 2.3 makes this swap transparent to the rest of the app.

#### 3.3 Enable release minification

`app/build.gradle.kts:20` has `isMinifyEnabled = false`. Before any production release:

```kotlin
buildTypes {
    release {
        isMinifyEnabled = true
        proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
}
```

Add a `proguard-rules.pro` keeping Compose-related reflection targets.

#### 3.4 Raise Java target

Update `app/build.gradle.kts:29–33` from `VERSION_1_8` to `VERSION_11`:

```kotlin
compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}
kotlinOptions { jvmTarget = "11" }
```

---

### Tier 4 — Future Features

These are ideas, not commitments. Each requires additional libraries.

| Feature | Dependencies needed | Notes |
|---------|-------------------|-------|
| **PokéAPI integration** | Retrofit, OkHttp, Coroutines | Show Pokémon sprites on quiz questions; requires network permission |
| **Statistics screen** | Room | Per-type accuracy over time; currently only total score per mode is stored |
| **Light/dark mode toggle** | None | Add `LightColorScheme` to `Theme.kt`; store preference in DataStore |
| **Accessibility** | None | Add `contentDescription` to all `TypeBadge` composables; add a color-meaning legend to `StudyScreen` |
| **Difficulty settings** | None | Filter question pool by type tier (common vs. rare matchups) |

---

## 4. Recommended Reading Order

For anyone new to the codebase:

1. `data/TypeChart.kt` — start here; all domain models and the type chart logic
2. `MainActivity.kt` — understand navigation and where top-level state lives
3. `ui/HomeScreen.kt` — simplest screen; shows the `SegmentedSelector` reuse pattern
4. `ui/StudyScreen.kt` — chart rendering; see how `getEffectiveness()` drives the grid
5. `ui/QuizScreen.kt` — most complex; read the state variables first (lines 85–101), then the quiz branch, then the results branch
