# Expressive Files — Compose contract (v1)

Read this before writing code. This is a parallel build: **file ownership is strict**.

## Stack (already on the classpath)

- Kotlin 2.4.20, AGP 9.4.1, minSdk 24, compileSdk 37
- `androidx.compose:compose-bom:2026.09.00` → Compose UI 1.12.1
- `androidx.compose.material3:material3:1.5.0-rc01` pinned explicitly (first-party
  expressive APIs; the BOM still pins the compatible Compose UI/runtime artifacts)
- activity-compose, lifecycle-runtime/viewmodel-compose, navigation-compose
- `androidx.compose.material:material-icons-core`
- Existing app: `me.zhanghai.android.files` (namespace/R). New code lives in
  `me.zhanghai.android.files.compose.*`.

## Rules

1. **Only edit files inside your assignment globs.** Never reformat or touch
   existing layouts, activities, adapters, themes, or `app/build.gradle`.
2. **No new dependencies without an explicit decision record.** The Material3
   `1.5.0-rc01` pin is approved; do not add others ad hoc.
3. Material3 **1.5.0-rc01** is the ceiling. Prefer first-party expressive APIs:
   `MaterialExpressiveTheme`, `MotionScheme.expressive()`, `ButtonGroup`,
   `LoadingIndicator`, `FloatingActionButtonMenu`, `SplitButton`, `MaterialShapes`.
   Do not use APIs from 1.6+ or replace a first-party component with a bespoke one
   without a documented reason.
4. Experimental APIs must be annotated:
   `@OptIn(ExperimentalMaterial3Api::class)` /
   `ExperimentalMaterial3ExpressiveApi` / `ExperimentalLayoutApi`.
5. New UI copy: plain English string literals with `// TODO: localize` on the
   same line. Do not invent `R.string` keys (they will not compile). If you use
   an existing key, verify it with grep first.
6. Use `me.zhanghai.android.files.R` for existing drawables, otherwise
   `Icons.Filled.*` from material-icons-core only:
   Add, ArrowBack, Check, Close, Delete, Edit, Folder, Home, Info,
   KeyboardArrowDown, KeyboardArrowRight, List, MoreVert, Person, Refresh,
   Search, Settings, Share, Warning.
7. Compose screens are **stateless**: `state` + lambda callbacks. No ViewModel,
   no LiveData, no Android `Context` beyond `LocalContext` for intents. The
   integrator wires real data.
8. Accessibility: every icon-only control gets a `contentDescription`; 48dp
   minimum touch targets; don't encode meaning with color alone.
9. Support light/dark via `MaterialTheme.colorScheme`; never hardcode colors.
   Use `MaterialTheme.shapes`; never hardcode corner radii unless a spec calls
   for it.
10. Prefer `LazyColumn`/`LazyVerticalGrid` with `key = { it.path }`.
11. Keep files under ~400 lines; split freely inside your package.
12. Add `@Preview(showBackground = true)` for the main screen of your package.

## Shared contracts

`me.zhanghai.android.files.compose.model` already contains:
`FileEntryUi`, `BreadcrumbUi`, `FileListUiState`, `FileLayoutMode`,
`DrawerEntryUi`, `DrawerSectionUi`, `NavigationUiState`, `ShortcutEntryUi`,
`SettingsItemUi`, `SettingsSectionUi`,
`SettingsUiState`, `OperationUi`, `ConflictUi`, `PropertyRowUi`,
`PropertySectionUi`, `PropertiesUiState`.

Favorites/home models (`HomeUiState`, `HomeSectionUi`, favorite flags,
star drawables, `FavoritesStore`) were removed; Locations is the hub for
storages/bookmarks. Theme-color/material3 settings rows were removed with
their `colorArgb` option dot.

Do not modify `model/UiModels.kt`. If something is missing, keep the data
inside your own package.

## Design tokens to use (resolved from MaterialTheme)

| Token | Value |
|---|---|
| Surface | `colorScheme.surface` |
| App bar / selected row container | `colorScheme.surfaceContainer` |
| Card / sheet | `colorScheme.surfaceContainerHigh` |
| Accent | `colorScheme.primary` / `onPrimary` |
| Tonal selection | `colorScheme.secondaryContainer` / `onSecondaryContainer` |
| Secondary text | `colorScheme.onSurfaceVariant` |
| Divider | `colorScheme.outlineVariant` |
| Destructive | `colorScheme.error` / `onError` |
| Shapes | extraSmall 4, small 8, medium 12, large 16, largeIncreased 20, extraLarge 28, extraLargeIncreased 32, extraExtraLarge 48 |
| Type | titleLarge, titleMedium, bodyLarge, bodyMedium, labelLarge |
| Spacing | 4, 8, 12, 16, 24, 32 |
| Heights | top bar 64, list row 72, grid tile 1:1, bottom bar 64, chip 32 |

## Package ownership

| Agent | Package / paths |
|---|---|
| theme | `compose/theme/**` |
| components | `compose/components/**` |
| filelist | `compose/features/filelist/**` |
| navigation | `compose/features/navigation/**`, `compose/features/home/**` |
| settings | `compose/features/settings/**`, `compose/features/onboarding/**` |
| viewers | `compose/features/viewer/**`, `compose/features/properties/**` |
| operations | `compose/features/operations/**` |
| state | `compose/state/**` |
| icons | `res/drawable/**`, `res/mipmap-*/**`, `res/values/colors_launcher.xml`, `art/**` |
| quality | `app/src/test/**`, `app/src/androidTest/**`, `.github/workflows/**`, `docs/**`, `README*` |

Everything else belongs to the integrator. Do not touch it.
