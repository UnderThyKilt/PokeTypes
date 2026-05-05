# PokeTypes — Future Features Backlog

## User-Requested

### 1. Simplified Pokédex

A new browse screen showing all 1025 Pokémon from `pokemon.json`.

**What the user sees:**
- Scrollable list: sprite + Pokédex number + name + type badge(s) per card
- Filter by type (tap a type badge to show only that type)
- Filter by generation (reuse existing `SpriteGeneration` enum)
- Search bar by name

**Already in place — no new data or dependencies needed:**
- All data is in `assets/pokemon.json` (`id`, `name`, `generation`, `types`)
- `AssetPokemonRepository` is already loaded and indexed at app start
- `TypeBadge`, `SubcomposeAsyncImage` / Coil pipeline already wired
- `SpriteGeneration` enum already exists for generation filtering

**New code needed:**
- `PokedexRoute` in `Routes.kt`
- `PokedexViewModel` (reads all Pokémon from repository, holds filter/search state)
- `PokedexScreen.kt` composable
- Button on `HomeScreen` or top-nav icon

**Critical files:** `Routes.kt`, `HomeScreen.kt`, `PokemonRepository.kt`, `TypeBadge.kt`

---

### 2. More Theme Options

Current state: one dark scheme and one light scheme, both hardcoded in `Theme.kt`. The `isDarkTheme: Boolean` in `AppSettings` only toggles between them.

**Proposed options:**

| Theme | Description |
|-------|-------------|
| Dark (current) | Deep blue-black, red primary |
| Light (current) | Off-white, red primary |
| AMOLED Black | Pure `#000000` backgrounds for OLED screens — saves battery |
| Type Accent | User picks a favorite Pokémon type; that type's `PokemonType.color` becomes the primary accent (18 built-in color options, zero new assets needed) |
| High Contrast | White/black with no mid-tones; accessibility-focused |

**Already in place:**
- `PokemonType.color` — every type already has a distinct color
- `PokeTypesTheme` composable in `Theme.kt` is the single wiring point
- `SegmentedSelector<T>` reusable for the theme picker UI

**New code needed:**
- Replace `isDarkTheme: Boolean` with a `ThemeChoice` enum in `AppSettings`
- Add optional `accentType: PokemonType?` to `AppSettings` for type-accent mode
- Update `PokeTypesTheme` to generate a `ColorScheme` from `ThemeChoice` + `accentType`
- Theme picker section in `SettingsScreen`

**Critical files:** `Theme.kt`, `Colors.kt`, `SettingsRepository.kt`, `SettingsScreen.kt`

---

## Additional User-Facing Features

### 3. Type Matchup Calculator *(small effort, high value)*

An interactive lookup tool: pick an attacking type and one or two defending types, instantly see the multiplier. No quiz — just a fast answer for players mid-game.

- `getEffectiveness()` does all the math; this is mostly UI work
- New `CalculatorScreen.kt` + `CalculatorRoute`
- Type grid of 18 `TypeBadge` buttons for selection
- Result shows the multiplier label and color-coded effectiveness

**Critical files:** `TypeChart.kt`, `Routes.kt`, `HomeScreen.kt`

---

### 4. Results Sharing *(small effort, social value)*

Share quiz results as a formatted string from `ResultsScreen` — similar to Wordle's output.

```
PokeTypes — Dual Type Quiz (Gen 6+, Hard)
Score: 8/10 · Great job! 🔥
🟢🟢🔴🟢🟢🟢🔴🟢🟢🟢
```

- `QuizState.questionResults: List<Boolean>` already has everything needed
- Android `Intent.ACTION_SEND` — zero new dependencies
- Share button added to `ResultsScreen`

**Critical files:** `ResultsScreen.kt`

---

### 5. Daily Challenge *(medium effort, engagement)*

A fixed daily quiz (10 questions, seeded from the current date — same questions for everyone on the same day). Score shown at end with a shareable result.

- Seed Kotlin's `Random(seed)` from `LocalDate.now().toEpochDay()`
- Separate score key (`"DAILY"`) in `ScoreRepository`
- Daily quiz button on `HomeScreen`

**Critical files:** `QuizLogic.kt`, `HomeScreen.kt`, `ScoreRepository.kt`

---

### 6. Weakness / Resistance Lookup *(small effort, utility)*

Given a defending type (or type pair), show all 18 attacking types sorted by effectiveness — the reverse of the study chart. Answers "what hits me hard?"

- `getEffectiveness()` already handles this
- Can be a tab or mode within `StudyScreen`, or a new route
- Zero new data needed

**Critical files:** `StudyScreen.kt`, `TypeChart.kt`

---

### 7. Sound & Haptic Feedback *(small effort, polish)*

- Haptic pulse on wrong answers via `LocalHapticFeedback` (already in Compose — zero new dependencies)
- Optional short sound effects for correct/wrong
- Toggle in Settings (default off)
- `soundEnabled: Boolean` added to `AppSettings`

**Critical files:** `QuizContent.kt`, `SettingsRepository.kt`, `SettingsScreen.kt`

---

## Technical / Code Quality

### 8. QuizViewModel Unit Tests *(medium effort, high value)*

All dependencies are now in `build.gradle.kts` (`kotlinx-coroutines-test`, `Turbine`, MockK) but no ViewModel tests exist yet. `QuizViewModel` is now cleanly injectable.

**What to test:**
- `selectAnswer()` updates `selected`, `streak`, `correctAnswers`
- `advance()` moves `questionIndex`, resets `selected` to null
- `endQuiz()` sets `quizComplete = true`
- Score saved to `ScoreRepository` on quiz completion
- Stats recorded via `TypeStatRepository` per question

**New file:** `QuizViewModelTest.kt` using `runTest` + Turbine's `test {}` on `state`

**Critical files:** `QuizViewModel.kt`, `ScoreRepository.kt`, `TypeStatRepository.kt`

---

### 9. Accessibility Pass *(small effort, correctness)*

Currently listed as a known limitation.

- Add `contentDescription` to all `TypeBadge` composables (e.g., `"Fire type"`)
- Add `contentDescription` to quiz answer buttons
- Add a color-meaning legend to `StudyScreen` for color-blind users

**Critical files:** `TypeBadge.kt`, `QuizContent.kt`, `StudyScreen.kt`

---

### 10. String Resources *(large effort, localization foundation)*

Move all hardcoded UI strings to `res/values/strings.xml`. Required for future localization support.

High-priority strings: quiz prompt labels, button labels, performance messages, settings labels.

**Critical files:** Most `ui/` files, `QuizLogic.kt`

---

## Priority Order

| # | Feature | Effort | User Value | Code Value |
|---|---------|--------|------------|------------|
| 1 | Simplified Pokédex | Medium | ★★★★★ | ★★★ |
| 2 | More theme options | Medium | ★★★★☆ | ★★★ |
| 3 | Type matchup calculator | Small | ★★★★☆ | ★★ |
| 4 | Results sharing | Small | ★★★☆☆ | ★★ |
| 5 | QuizViewModel tests | Medium | ★★☆☆☆ | ★★★★★ |
| 6 | Weakness lookup | Small | ★★★☆☆ | ★★ |
| 7 | Accessibility pass | Small | ★★★☆☆ | ★★★★ |
| 8 | Daily challenge | Medium | ★★★☆☆ | ★★★ |
| 9 | Sound & haptic | Small | ★★☆☆☆ | ★★ |
| 10 | String resources | Large | ★☆☆☆☆ | ★★★★ |
