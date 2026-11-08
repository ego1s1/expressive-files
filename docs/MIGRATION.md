# Compose migration tracker

Expressive Files keeps the proven Material Files backend while replacing the UI surface
by surface. This document records what is migrated, what is bridged to a legacy
Activity/Fragment, and what is still pending. It is updated in the same change that
flips an entry point.

**Snapshot:** 2026-11-03, fork base Material Files `1.7.5`, fork version `0.1.0`
(versionCode 1), application id `dev.expressive.files`.

## Status definitions

| Status | Meaning |
|---|---|
| **migrated** | The Compose screen is the production entry point. No legacy implementation is used for normal navigation. |
| **bridged** | A Compose implementation exists, but at least one production path still launches the legacy screen. Both sides must be kept working until the bridge is removed. |
| **pending** | The legacy screen is still authoritative. A Compose target package is reserved, but no production path uses it yet. |

A surface is only **migrated** when it has real data wiring, deep-link parity, selection
and operation callbacks, process-death handling, accessibility checks, and tests. A
Compose preview alone is not a migration.

## Tracker

| Legacy surface | Current implementation | Compose target | Status | Notes |
|---|---|---|---|---|
| Launcher / app shell | `app/AppActivity`, `compose/MainActivity` | `compose/`, `compose/theme/` | **migrated** | Compose `MainActivity` is the launcher; theme, edge-to-edge, file list, bottom navigation, settings, about and basic properties are routed. Legacy screens remain reachable where noted below. |
| File list | `filelist/FileListFragment`, `FileListAdapter`, `FileListViewModel` | `compose/features/filelist` | **migrated** | Compose list/grid, search, selection, FAB menu, create/rename/delete and breadcrumbs use the existing `FileListViewModel`. Phase 2 slims the top bar to back/title/subtitle only: search becomes a pill destination and sort, list/grid and refresh move to breadcrumb-row chips. Legacy `FileListActivity` stays exported only for external `VIEW`/picker intents. |
| Breadcrumbs | `filelist/BreadcrumbLiveData` | `compose/features/filelist` | **migrated** | Compose breadcrumb rail with current pill and horizontal scroll; legacy layout remains for external legacy screens. Only the trail up to the current folder is shown — forward history left by navigate-up is dropped so the path is always correct. |
| Drawer / navigation | retired | `compose/features/navigation/BottomPillNav.kt`, `PillNavRail` | **migrated** | Floating pill on phones (Files/Locations/Settings) and a vertical pill rail at ≥600dp. Search moved to the app-bar; Recents/history and Favorites removed. Drawer and related plumbing are removed. See [NAVIGATION.md](NAVIGATION.md). |
| Bookmarks | legacy home section under `navigation/` | `compose/features/navigation` + settings sub-screens | **migrated** | Storages and bookmarks live in Locations with native add/edit/delete/toggle sub-screens in settings. |
| Settings | retired legacy menu (`SettingsActivity` now redirects) | `compose/features/settings` | **migrated** | Every legacy settings row is native: switches, choice dialogs (locale/night/ellipsize/root/encoding/APK action), default-folder dialog, storages/bookmarks/standard-folder sub-screens and reset. Legacy `SettingsActivity` only survives as a redirect for the system preferences action; the preference fragments, `settings.xml` and the storage/bookmark/standard-directory list activities are deleted. Server credential editors stay legacy (see Servers row). |
| Onboarding / permissions | legacy first-run and rationale dialogs | `compose/features/onboarding` | **bridged** | A Compose rationale sheet exists but is not wired. Storage permission and SAF flows must keep the exact request/result contract. |
| Properties | `fileproperties/` tabs | `compose/features/properties` | **bridged** | Basic file properties are routed from the file action sheet; checksum, permission, APK and media tabs still use the legacy dialog. |
| Viewers (text / image / media / archive) | `viewer/` activities and fragments | `compose/features/viewer` | **bridged** | Image files open the Compose viewer; text, media, PDF and archive viewing still use legacy/external intents until parity and gestures are proven. |
| Operations / conflicts | `filejob/`, `filelist/` conflict dialogs | `compose/features/operations` | **bridged** | Create/rename/delete use Compose confirmation UI and the frozen job engine; archive, conflict and batch-progress sheets still need wiring. |
| Servers (SMB / FTP / SFTP / WebDAV) | `storage/` server editors, providers under `provider/` | `compose/features/settings` (server management); extraction to `compose/features/servers` requires integrator approval | **pending** | UI only. Provider implementations and credentials storage are frozen. |
| FTP server | `ftpserver/FtpServerActivity`, `FtpServerFragment`, service and tile | `compose/features/settings` (FTP section); extraction to `compose/features/ftpserver` requires approval | **pending** | Service, notification, tile and receiver stay legacy; only the control screen moves. |
| About / licenses | `about/AboutActivity`, `AboutFragment` | `compose/features/settings` | **bridged** | A Compose about screen exists but is not routed. Keep GPLv3 and upstream attribution visible; do not move license text behind a web-only link. |

## Navigation migration

The shell is moving from a modal drawer to a floating destination pill, then to an
adaptive rail. [`NAVIGATION.md`](NAVIGATION.md) owns the detailed destination map,
geometry and component list; this tracker only records phase state.

1. **Phase 1 — floating icon-only pill (shipped).** `BottomPillNav` owns
   Files/Locations/Settings with no persistent labels, ≥48dp targets,
   selected-state semantics and tooltips. It is the production navigation on phones;
   production path; the drawer remains behind the flag as the fallback. The pill is
   hidden during selection, search, viewers and properties.
2. **Phase 2 — Search as a destination (next).** Search becomes a fifth pill
   destination. The file-list top bar keeps only back/title/subtitle; search, sort,
   refresh and overflow leave the app bar. Sort and list/grid move to chips on the
   breadcrumb row, with refresh behind that row's overflow and a pull-to-refresh
   gesture. Expected files: `SearchScreen`/`SearchStates`, `BreadcrumbRow`,
   `BreadcrumbActions`, plus edits to `BottomPillNav`, `FileListTopBar`,
   `FileListScreen` and the integrator-owned `ExpressiveFilesApp`.
3. **Phase 3 — retire the drawer and enable the rail (done).** `AppDrawer`, the
   fallback flag and `onOpenDrawer` plumbing are deleted. Promote
   `AppNavigationRail` at widths ≥600dp with Files/Locations/Settings
   plus Settings/About; keep Locations as the storage/bookmark hub.
4. **Phase 4 — polish.** Use the theme motion tokens for pill selection and route
   transitions, add predictive back in the existing back-priority order, and verify
   RTL, 200% font scale, TalkBack, IME and FAB/navigation-bar clearance.

**Selection rule:** whenever a selection exists, `SelectionBar` replaces the
file-list top bar and the pill/rail stays hidden. Selection never changes the route
and never turns a destination into an action; closing the selection restores the
browse chrome.

## Frozen backends

The following are the stable backend and are **frozen for the UI migration**:

- `provider/**` — Linux, root, content, document, archive, FTP, SFTP, SMB and WebDAV
  providers.
- `filejob/**` — copy, move, delete, archive, extraction and conflict engine.
- `ftpserver/**` — the FTP server service, receiver, tile and notification plumbing.
- `storage/**` — saved servers, bookmarks and credential persistence.
- `file/**` — NIO2/syscall file abstractions.

"Frozen" means no renames, signature changes, behavior changes, thread-model changes or
refactors made for UI convenience. Compose state holders must adapt to the existing
repositories, `LiveData`/`Flow` sources and job APIs. Bug fixes are still allowed, but
they need their own tests and must not be bundled into a visual migration change.

The UI must not re-implement file operations. A Compose screen may request an operation
or observe its state; the job engine remains the single writer.

## Bridging rules

While a surface is **bridged**:

1. Keep the legacy Activity/Fragment exported until every external intent, shortcut,
   tile and picker caller has a Compose equivalent.
2. Preserve intent extras, result codes and URI permission grants exactly. Document any
   unavoidable difference in the migration PR.
3. Compose entry points are the default only behind an explicit flag or a deliberate
   navigation change; do not silently route some paths to Compose and others to legacy.
4. Both paths must share the same state source. Do not create a second cache of file
   entries or server credentials.
5. Back navigation, process death and deep links are tested on the bridged path, not
   only on the Compose path.

## Definition of done

A surface may move from **pending** or **bridged** to **migrated** only when:

- the screen is stateless (`state` + lambdas) and previews exist for light/dark;
- real data replaces fixture data, including empty, loading, error and permission-denied
  states;
- selection, sorting, search, paste and operation callbacks match the legacy behavior;
- external intents and deep links land on the same destination;
- TalkBack, 200% font scale, RTL and compact/medium/expanded widths are checked;
- unit tests cover any new pure mapping, and a manual test record is attached to the PR;
- the legacy screen is either deleted or explicitly retained with a documented reason.

## Order of work

1. **Shell and read-only surfaces** — theme, file list, breadcrumbs, drawer, home,
   properties, viewers. No behavior change to the job engine.
2. **Mutating file actions** — rename, create, delete, copy/move, archive, extract;
   conflict handling stays on the existing engine.
3. **Remote and server management** — server editors and FTP control, with providers
   untouched.
4. **Retire bridges** — remove legacy launcher paths, then external intents, one
   surface at a time. Keep a release with both paths available before deletion.

## Known risks

- Directory `VIEW` intents and document pickers can arrive from other apps with
  transient URI grants; a Compose-only route must reproduce the grant handling.
- Root and archive providers rely on process-wide state and long-running jobs; do not
  move them into a screen-scoped ViewModel.
- The FTP service and tile are separate entry points from the UI; they must continue to
  work while the control screen is migrated.
- Saved server credentials and bookmarks live in `storage/**`; migration must not
  rewrite their on-disk format.
