# Changelog

## [Unreleased] - 2026-05-04

### Removed
- Deleted `ScoreHistory.kt` — dead code superseded by `DataStoreScoreRepository`
- Deleted `ThemeRepository.kt` — dead code superseded by `SettingsRepository`

### Changed
- Added `kotlinx.serialization 1.7.3` to README.md Tech Stack table to match ARCHITECTURE.md

---

## [Unreleased] - 2026-05-04

### Added
- Expanded Pokémon sprite pool from 291 curated entries to all 1025 base-form Pokémon (Gen 1–9)
- `SpriteGeneration` setting (Gen 1–8 or All) to filter which generation of Pokémon sprites appear in quiz
- Questions with no matching Pokémon for the selected generation are automatically skipped
- `hasPokemonForTypes()` deterministic lookup function for generation-aware question filtering
- `generation` field on `PokemonEntry` (1–9) to support generation-aware filtering at runtime
- `test/generate_pokemon_data.py` — fetches all Pokémon from PokéAPI and regenerates `TypePokemon.kt`
- `test/check_sprites.py` — verifies all PokemonEntry sprite URLs return HTTP 200
- Sprite error fallback: shows "?" placeholder instead of blank when a sprite fails to load
- Pokédex number displayed next to Pokémon name in quiz cards (e.g. `#6 Charizard`)

### Fixed
- Blank sprite display when a specific Pokémon sprite fails to load (now shows placeholder)

### Changed
- `TypePokemon.kt` is now auto-generated; `randomPokemonForTypes` accepts `maxGeneration` parameter
- `PokemonEntry` gains a `generation: Int` field
- Sprite generation setting only appears in Options when Style is set to Pokémon

---

## [Unreleased] - 2026-05-04

### Added
- Added light/dark theme toggle, persisted in DataStore
- Added Difficulty setting (Normal = 10 core types, Hard = all generation types)
- Added Quiz Length setting (5, 10, 20, or Endless questions)
- Added Endless quiz mode: questions generated one at a time, score not saved to history
- Added Options screen consolidating all settings (theme, generation, difficulty, length, style)
- Added Stats screen with per-type attacking/defending accuracy bars backed by Room
- Added no-duplicate question generation within a quiz session
- Added Pokémon mode: quiz questions show a Pokémon sprite, name, and type badge instead of a plain badge
- Added `PresentationMode` setting (Classic / Pokémon), selectable from the Options screen
- Added Coil 3.1.x image loader with OkHttp network fetcher and persistent 50 MB disk cache in `filesDir`
- Added `PokeTypesApp` Application subclass to configure the singleton Coil image loader
- Added hardcoded Pokémon roster (~7 per type, all 18 types) in `data/pokemon/TypePokemon.kt`

### Changed
- Consolidated all settings into a single `SettingsRepository` / `AppSettings` data class
- `QuizRoute` now carries difficulty, quiz length, and presentation mode as typed route arguments
- `ResultsScreen` score display and history section adapt to fixed vs. endless quiz length
- Updated `README.md` and `docs/ARCHITECTURE.md` to reflect current feature set and tech stack

---

## [Unreleased] - 2026-05-04

### Added
- Introduced `QuizViewModel` with `StateFlow`-based UI state management
- Added type-safe navigation with a dedicated `Routes` sealed class
- Added `QuizContent` composable and `ResultsScreen` for a cleaner quiz flow
- Added `QuizLogic` for isolated quiz business logic
- Added `ScoreRepository` backed by DataStore for persistent score history
- Added `TypeBadge` reusable composable component
- Added unit tests for `QuizLogic`, `ScoreHistory`, and `TypeChart`
- Added architecture documentation (`docs/ARCHITECTURE.md`) and README
- Added text outline effect on quiz answer buttons for improved readability

### Changed
- Refactored `QuizScreen` by extracting logic and UI into dedicated components
- Refactored `MainActivity` to use type-safe navigation
- Updated `HomeScreen` with score history display
