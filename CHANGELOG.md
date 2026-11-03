# Changelog

All notable changes to Expressive Files are documented here.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).
The fork starts from Material Files `1.7.5` (versionCode 40); release versions will be
declared when the first standalone build is tagged.

## [Unreleased]

### Added

- Compose-first launcher shell (`compose/MainActivity`) with edge-to-edge layout and a
  dedicated theme entry point.
- Shared stateless UI models for the file list, breadcrumbs, navigation drawer, home,
  settings, operations, conflicts and properties (`compose/model/UiModels.kt`).
- Compose screen workstreams for file list, navigation/home, settings/onboarding,
  properties/viewers and operations, built against Material3 1.4.0 and the Compose BOM.
- JVM unit tests for `FileEntryUi`, `FileListUiState` and `FileLayoutMode` defaults,
  copy semantics and selection-count consistency.
- Design system documentation with the Material 3 Expressive token map, component
  rules and review checklist.
- Migration tracker for legacy surfaces, including the frozen provider/job boundary.
- Testing guide covering unit tests, the future screenshot matrix, and manual
  accessibility, RTL and font-scale checklists.

### Changed

- Application id changed to `dev.expressive.files`; the app now presents as
  Expressive Files with a new launcher icon and monochrome adaptive icon.
- CI now runs on JDK 21 for both push and pull requests, executes
  `:app:testDebugUnitTest :app:assembleDebug -x lint`, uploads the debug APK and test
  reports, and runs lint in a separate non-blocking job.
- Upstream version `1.7.5` (versionCode 40) is retained as the fork baseline.

### Removed

- Firebase, Crashlytics and the `NONFREE` telemetry integration path.
- Android TV/leanback support and its TV-specific assets.
- The Firebase configuration file and related build plugins.

### Security

- No analytics or crash-reporting SDK is bundled in this fork. Diagnostics must be
  provided explicitly by the user.

## Fork lineage

Expressive Files is an independent GPLv3 fork of
[Material Files](https://github.com/zhanghai/MaterialFiles) by Hai Zhang. Upstream
copyright and license notices are retained. See `README.md` and `LICENSE`.
