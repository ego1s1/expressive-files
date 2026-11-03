# Navigation

**Snapshot:** 2026-11-03. Compose-first shell.

## Model

- **Phone:** floating pill navigation at the bottom with **Files · Favorites · Locations · Settings**.
- **Medium/expanded:** the same destinations become a vertical `PillNavRail` (≥600dp).
- **Search** is not a destination. It lives behind the search icon in the file-list app bar.
- **History/Recents page: removed.** There is no `recents` route and no recents store.
- The **create FAB** sits to the right of the pill on the Files route and is hidden everywhere else.
- Selection mode keeps its contextual top bar and hides both the pill and FAB.

## Header rules

- One owner for the current location: the **app-bar title**.
- The header uses the same `surface` color as the list and stays 64dp tall.
- **Root:** no breadcrumb row.
- **Subfolder:** a 40dp plain-text ancestor trail appears under the header. It contains ancestors only
  (never the current folder), with text + chevrons, no chips/pills.
- Trailing header icons: **Search**, **Sort**, **View options**.
- Refresh is a row inside the View options sheet, not a header icon.

## Floating chrome

- Content scrolls **behind** the pill and FAB; scrollable lists carry 112dp bottom content padding
  so every row can still be reached.
- FAB and pill share the same bottom baseline; the pill shifts to leave a 12dp gap.
- The selected destination expands to show its label on ≥390dp windows. Narrower windows stay
  icon-only and rely on content descriptions/tooltips.
- Route changes use a short fade + horizontal slide. The FAB is included in the exiting Files
  screen, so it scales/fades with the transition.
- The pill/FAB animate in and out with `AnimatedVisibility` when selection changes.

## Removed

- `AppDrawer`, `AppNavigationRail`, `ModalNavigationDrawer`, drawer fallback flag.
- `RecentsStore` and the Recents screen.
- Search as a bottom destination.
- Colored breadcrumb chips and breadcrumb-row action buttons.
- The separate sort chip.

## Files

| Responsibility | File |
|---|---|
| Destinations + pill/rail | `compose/features/navigation/BottomPillNav.kt` |
| Locations hub | `compose/features/navigation/LocationsScreen.kt` |
| Header | `compose/features/filelist/FileListTopBar.kt` |
| Ancestor trail | `compose/features/filelist/Breadcrumbs.kt` |
| Shell/routes/transitions | `compose/ExpressiveFilesApp.kt` |
| Search | `compose/features/search/SearchScreen.kt` |

## Invariants

- No screen repeats the current folder outside the title.
- No global chrome in the app bar besides Search, Sort, and View options.
- Pill destinations are top-level only; storage/bookmark selection stays inside Locations.
- Every icon-only control has a content description; minimum target 48dp.
- Settings is its own destination; About is reached from Settings.
