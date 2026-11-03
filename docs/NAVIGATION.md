# Navigation migration

This document is the navigation contract for the Compose shell. It records the
shipped Phase 1 floating pill, the Phase 2 plan that moves browse actions out of the
app bar, and the Phase 3–4 work that retires the drawer and adds motion. It is
subordinate to [`DESIGN.md`](DESIGN.md) for tokens and to
[`COMPOSE_CONTRACT.md`](COMPOSE_CONTRACT.md) for package ownership.

**Snapshot:** 2026-11-03, Phase 1 shipped behind `USE_BOTTOM_PILL_NAV = true`.

## Principles

- Global destinations live in the pill (compact) or the rail (medium and wider).
  Screen-local actions never become destinations.
- Route strings are owned by the shell (`ExpressiveFilesApp.kt`), not by feature
  packages. A screen receives state and callbacks; it does not navigate itself.
- The pill is icon-only. Tooltips and content descriptions carry the labels.
- Selection mode is contextual chrome and always outranks destination chrome.
- The drawer is a fallback only. New navigation work must not add drawer entries.

## Phase 1 — icon-only floating pill (current)

`compose/features/navigation/BottomPillNav.kt` renders a floating circular surface
with four destinations and no persistent labels.

| Destination | Route | Icon source | Accessible label |
|---|---|---|---|
| Files | `files` | `ExpressiveIcons.Folder` | Files |
| Recents | `recents` | `Icons.Filled.Refresh` | Recents |
| Favorites | `favorites` | `Icons.Filled.Star` | Favorites |
| Locations | `locations` | `R.drawable.device_icon_white_24dp` | Locations |

Geometry and behavior:

- Surface: `CircleShape`, `surfaceContainerHigh`, 3dp shadow, nested in a 4dp row
  with 4dp gaps.
- Targets: minimum 48×48dp per destination, 20dp icon, visual selection disc inside
  the target. No label is reserved or measured.
- Selected treatment: `secondaryContainer` disc with `onSecondaryContainer` icon;
  unselected icons use `onSurfaceVariant`. Selection is also exposed through
  `semantics { selected = … }`, not color alone.
- Each destination is a `TooltipBox` + `PlainTooltip` with the same text as its
  `contentDescription`.
- The pill floats above `navigationBarsPadding()` with a 16dp bottom margin. The
  route content is padded 88dp while the pill is visible so the last row is not
  covered (`LocationsScreen` reserves its own 112dp list padding).
- The pill is hidden while `selectedCount > 0`, while the inline search field is
  expanded, and while a viewer or properties screen is open.

### Drawer fallback

`ExpressiveFilesApp.kt` gates the whole migration on:

```kotlin
private const val USE_BOTTOM_PILL_NAV = true
```

- `true`: the pill is the production navigation. The drawer is not composed,
  drawer gestures are disabled, and screens receive `onOpenDrawer = null`.
- `false`: `ModalNavigationDrawer` + `AppDrawer` return, drawer gestures are enabled
  on the Files route, and the top bar/home screens get an `onOpenDrawer` callback.

The flag is temporary. Phase 3 removes it rather than adding a second navigation
implementation beside it.

### Selection rule

**Selection mode keeps its contextual top bar.** When `FileListUiState.selectedCount > 0`,
`FileListScreen` renders `SelectionBar` instead of `FileListTopBar`. The active file
list top bar is therefore never clickable underneath a selection, and the pill is
hidden for the duration of the selection. Closing the selection restores the normal
top bar and the bottom destination set; it never changes the route. Search cannot be
entered from the selection bar. Chips added in Phase 2 are browse chrome and are
hidden or non-interactive while the contextual bar is up.

## Phase 2 — Search as a destination, slim app bar (next)

### What changes

1. **Search becomes a fifth pill destination** (`search`), placed after Files and
   before Recents. Selecting it opens the search surface for the current directory;
   leaving it restores the previous Files path. Search remains a destination while a
   query is active, and the existing `FileListUiState.searchQuery` / `isSearching`
   fields stay the source of truth for the query and for the legacy
   `FileListViewModel.search()` bridge.
2. **The app bar shrinks to back / title / subtitle.** `BrowseTopBar` keeps the
   navigation affordance (back when `canNavigateUp`, otherwise no drawer icon once
   the drawer is gone), the title, and the optional subtitle. Search, sort, refresh,
   and overflow icons are removed from the app bar; the inline `SearchTopBar` branch
   is removed with them.
3. **Sort and view options move to breadcrumb-row chips.** A trailing chip group sits
   beside the scrollable breadcrumb trail:
   - `Sort` chip — label reflects the current sort (`state.sortLabel`), opens the
     existing `SortSheet`.
   - `View` chip — toggles list/grid and reflects `state.layoutMode`.
   - Overflow chip — hosts Refresh and any remaining browse actions that do not
     deserve a permanent chip. Pull-to-refresh on the list remains the primary
     refresh gesture.
   Chips are 32dp visual height with a 48dp minimum touch target, labelled for
   TalkBack, and must not be placed inside the breadcrumb `LazyRow` (they would
   scroll away with the trail). Refresh must not be the only way to recover from an
   error state: the existing error retry action stays.
4. **Search results reuse file-list primitives.** `FileEntryUi`, selection behavior,
   and the frozen file-operation callbacks stay identical so a result row supports
   long-press selection, the action sheet, and delete/share without a second model.

### Expected component files

| File | Change | Owner note |
|---|---|---|
| `compose/features/navigation/BottomPillNav.kt` | Add the `search` destination (label + tooltip). | Navigation package. |
| `compose/features/search/SearchScreen.kt` | New stateless search surface: query field, empty/loading/error/results states, selection callbacks. | New package; do not add a ViewModel. |
| `compose/features/search/SearchStates.kt` | New screen-local state for the search field/results if `FileListUiState` is not sufficient. | Keep out of `model/UiModels.kt`. |
| `compose/features/filelist/FileListTopBar.kt` | Remove search/sort/overflow actions and `SearchTopBar`; keep back/title/subtitle only. | Filelist package. |
| `compose/features/filelist/BreadcrumbRow.kt` | New row that composes `Breadcrumbs` plus the trailing chip group. | Keeps `Breadcrumbs.kt` a pure trail. |
| `compose/features/filelist/BreadcrumbActions.kt` | New `Sort` / `View` / overflow chips and their menus. | Filelist package. |
| `compose/features/filelist/FileListScreen.kt` | Host the slim top bar and breadcrumb row; drop search/sort callbacks that moved. | Filelist package. |
| `compose/features/filelist/SortSheet.kt` | Reused as-is, launched from the Sort chip. | No behavior change. |
| `compose/ExpressiveFilesApp.kt` | Add `ROUTE_SEARCH`, hoist query state to the route, remove `searchExpanded` from the pill-visibility gate, wire chips. | Integrator-owned; requires approval. |
| `compose/state/FileListStateMapper.kt` | No schema change expected; `sortLabel` and `isSearching` already exist. | State package. |

Search route rules:

- Back from Search clears the query, calls `stopSearching()`, and returns to Files.
  Back must not replay the previous search when the user re-enters Search.
- The pill remains visible on the Search route so another destination is always one
  tap away; the screen is responsible for IME padding so the field and results are
  not covered.
- Selection inside search uses the same `SelectionBar` rule. The pill is hidden while
  a search result is selected.

## Phase 3 — retire the drawer, add the rail at ≥600dp

- Delete `compose/features/navigation/AppDrawer.kt` and its preview; remove
  `ModalNavigationDrawer`, `drawerState`, `USE_BOTTOM_PILL_NAV`, and every
  `onOpenDrawer` parameter from the shell, `FileListScreen`, `FileListTopBar`, and
  `HomeScreen`.
- Promote `AppNavigationRail.kt` from orphaned component to production navigation for
  window widths **≥600dp**. The rail uses `NavigationRailItem` with visible labels
  and keeps the 80dp collapsed width. It must gain the `search` and `locations`
  destinations (today it only repeats Files/Recents/Favorites) and preserve the
  Settings/About footer actions, which move from the drawer's bottom rows.
- Add `compose/features/navigation/AdaptiveNavigation.kt` to choose between the pill
  and the rail from one breakpoint decision. Read the width from the existing layout
  APIs (`BoxWithConstraints` / `LocalConfiguration`); do not add an adaptive
  navigation dependency.
- Keep `NavigationStateBuilder.kt` and `LocationsScreen.kt`: Locations remains the
  storage/bookmark hub that replaces the drawer's shortcut sections. The builder
  still feeds `DrawerEntryUi`/`DrawerSectionUi`; the model names are historical and
  must not be renamed in this phase.
- Transition at 600dp may be a hard swap in Phase 3. Animated width/visibility
  changes belong to Phase 4.

Exit criteria: no drawer code path remains, the pill works below 600dp, the rail works
at 600dp and above, and every route previously reachable from the drawer is reachable
from the pill/rail or from Locations.

## Phase 4 — polish, motion, predictive back

- Use the existing `compose/theme/Motion.kt` tokens for the pill's selected-disc
  spring, route content crossfade/shared-axis transitions, and the
  sort/list chip morph. Respect the system animator scale and the existing
  `Settings.FILE_LIST_ANIMATION` preference; reduce-motion must not remove state
  changes.
- Add predictive back for the in-app stack: viewer/properties dismiss, selection
  clear, Search exit, nested directory up, and route return to Files. The drawer's
  predictive-back behavior disappears with the drawer in Phase 3. Back priority must
  match the current `BackHandler` order: overlays first, then selection, then Search,
  then settings/about, then routes, then directory up.
- Polish the pill/rail handoff (no layout jump at 600dp), tooltip timing, RTL
  mirroring, 200% font scale, and TalkBack state descriptions for the chips.
- Verify the pill never overlaps the FAB, the IME, the navigation bar, or the last
  list row at any width or font scale.

## Destination and action map

| Surface | Route / id | Phase 1 | Phase 2+ | Phase 3 rail | Notes |
|---|---|---|---|---|---|
| Files | `files` | Pill | Pill | Rail | Root of the browse stack. |
| Search | `search` | App-bar icon | **Pill** | Rail | Same query/selection model as the file list. |
| Recents | `recents` | Pill | Pill | Rail | Backed by `RecentsStore`. |
| Favorites | `favorites` | Pill | Pill | Rail | Backed by `FavoritesStore`. |
| Locations | `locations` | Pill | Pill | Rail | Storages, bookmarks, Settings, About. |
| Settings | `action_settings` | Locations | Locations | Rail footer + Locations | Never a pill destination. |
| About | `action_about` | Locations | Locations | Rail footer + Locations | Never a pill destination. |
| Breadcrumb | path string | Breadcrumb row | Breadcrumb row | Breadcrumb row | In-screen navigation; does not change the pill selection. |
| Sort | action | App bar | Breadcrumb **Sort chip** → `SortSheet` | Same | Chip label shows the active sort. |
| List / grid | action | App-bar overflow | Breadcrumb **View chip** | Same | Reflects `state.layoutMode`. |
| Refresh | action | App-bar overflow | Breadcrumb overflow chip + pull-to-refresh | Same | Error retry remains on the error state. |
| Browse overflow | action | App-bar overflow | Breadcrumb overflow chip | Same | Only actions that do not deserve a chip. |
| Create / paste | action | FAB | FAB | FAB | Hidden in selection and search. |
| Selection actions | action | `SelectionBar` | `SelectionBar` | `SelectionBar` | Never in the pill or rail. |

## Invariants to keep in tests

- A zero-selection browse state has `selectedCount == 0`, no `FileEntryUi` with
  `isSelected = true`, and keeps the pill eligible (not searching, not in a viewer).
- Selection swaps the top bar; it does not change the route or turn a destination
  into an action.
- Destination order is stable: Files, Search (Phase 2+), Recents, Favorites,
  Locations. `DrawerSectionUi` and `NavigationUiState.sections` preserve builder order.
- Every icon-only destination or chip has a content description and a ≥48dp target.
