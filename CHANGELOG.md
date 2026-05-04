# Changelog

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
