# Changelog

All notable changes to Expressive Files are documented here.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).
The fork starts from Material Files `1.7.5` (versionCode 40); release versions will be
declared when the first standalone build is tagged.

## [Unreleased]

### Removed

- Favorites/home: `favorites` route, `HomeScreen`/`ShortcutRow`,
  `FavoritesStore`, star drawables, favorite action-sheet rows and their
  strings.
- Settings theme-color / Material3 rows (`appearance_theme_color`,
  `appearance_material3`) and `colorArgb` choice dots.

### Changed

- Breadcrumb is a full trail ending in the current folder (current crumb
  highlighted, inert); row height minimum is 48dp.
- Create menu state is hoisted: circular expressive button beside a centered
  pill/FAB group with caller-owned tap-outside scrim; FAB shows on Files only
  outside selection/search/loading/error.
- Predictive back tracks the system swipe edge (RTL-aware), gates motion on the
  animation setting, and suppresses the duplicate route slide on commit.

## [1.1.0-alpha02] - 2026-11-08

### Added

- Native settings sub-screens for storages, bookmarks and standard folders with
  add/edit/delete and toggle support.
- Choice dialogs for locale, theme color, night mode, file-name style, root
  strategy, archive encoding and APK action; default-folder dialog with
  current-folder and reset actions; reset-all confirmation.
- Direction-aware pane transitions (forward/back, RTL-mirrored) and mirrored
  predictive-back drag.

### Changed

- Every legacy settings row is wired natively; the legacy settings menu is
  retired (`SettingsActivity` now redirects to the Compose settings screen) and
  its fragments, preference classes and list activities are deleted.

### Fixed

- Breadcrumb no longer shows stale forward history after navigating up.
- Empty-state and shortcut-row headlines now use theme colors in dark mode.

## [1.1.0-alpha01] - 2026-11-07

### Changed

- FAB menu: spring-based icon rotation, staggered item entrances (nearest the FAB
  first) and a fast single-shot dismissal.
- Route transitions use the expressive emphasized easing curves (now defined in
  `Motion.kt` so custom motion never depends on alpha-only easing APIs).
- Navigation pill selection color follows the expressive effects motion spec.
- Dependencies: Compose backend on the alpha BOM (`2026.11.00`), activity
  `1.14.0-alpha03`, lifecycle `2.12.0-alpha05`, sshj `0.41.2`, guava
  `33.8.0-android`, Robolectric `4.17`. Material3 stays pinned to `1.5.0-rc01`,
  still the newest M3 release.
- Version bumped to `1.1.0-alpha01` (versionCode 2).

### Fixed

- Image preview downsamples large photos (max 2048px) instead of decoding them at
  full resolution.
- Favorites no longer retry disk reads on every access after a failed load.
- Back arrows and disclosure chevrons mirror correctly in RTL locales.
- Completed `ar` and `de` Compose translations (`compose_delete_1_item`,
  `compose_text`).

## [1.0.0] - 2026-11-06

### Added

- Material 3 Expressive theme with `MaterialExpressiveTheme`, expressive motion,
  20/32/48dp shape steps, true black and dynamic color.
- Dynamic color and animation settings wired into the Compose shell.
- Compose UI tests for selection semantics, row clicks and true-black surfaces.
- Release runbook (`docs/RELEASE.md`) and a Material 3 Expressive audit
  (`docs/M3_EXPRESSIVE_AUDIT.md`).

### Changed

- Material3 upgraded to 1.5.0-rc01; minimum SDK is now 24.
- All shipping Compose strings are externalized in `strings_compose.xml`.
- Selection is exposed with a check icon and semantics, not only a container color.
- File rows, bars and sheets grow with font scale instead of clipping.
- Store title and app branding are now Expressive Files.

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
