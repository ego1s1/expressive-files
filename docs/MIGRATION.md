# Compose migration tracker

Expressive Files keeps the proven Material Files backend while replacing the UI surface
by surface. This document records what is migrated, what is bridged to a legacy
Activity/Fragment, and what is still pending. It is updated in the same change that
flips an entry point.

**Snapshot:** 2026-11-03, fork base Material Files `1.7.5` (versionCode 40), application
id `dev.expressive.files`.

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
| Launcher / app shell | `app/AppActivity`, `compose/MainActivity` | `compose/`, `compose/theme/` | **migrated** | Compose `MainActivity` is the launcher; theme wrapper and `enableEdgeToEdge()` are in place. The root is still a placeholder until navigation is routed. |
| File list | `filelist/FileListActivity`, `FileListFragment`, `FileListAdapter`, `FileListViewModel` | `compose/features/filelist` | **pending** | `compose/state/FileListStateMapper` exists, but no Compose list is routed yet. Legacy activity remains exported for `VIEW` directory intents and picker flows. |
| Breadcrumbs | `filelist/BreadcrumbLayout`, `BreadcrumbLiveData` | `compose/features/filelist` | **pending** | Model is shared (`BreadcrumbUi`); rendering and scroll-to-current still legacy. |
| Drawer / navigation | `navigation/` fragments, legacy drawer layout | `compose/features/navigation` | **pending** | Destination ids and saved-state behavior must match before the legacy drawer is removed. |
| Home / bookmarks / recents | legacy home section under `navigation/` | `compose/features/home` | **pending** | `HomeUiState`, `HomeSectionUi`, `ShortcutEntryUi` already exist. |
| Settings | `settings/` fragments and preference classes | `compose/features/settings` | **bridged** | Compose settings components exist but are not routed; preference storage is frozen. |
| Onboarding / permissions | legacy first-run and rationale dialogs | `compose/features/onboarding` | **bridged** | A Compose rationale sheet exists but is not wired. Storage permission and SAF flows must keep the exact request/result contract. |
| Properties | `fileproperties/` dialog and tab fragments | `compose/features/properties` | **pending** | `PropertiesUiState` exists; no Compose screen yet. Checksum, permission, APK and media tabs need parity. |
| Viewers (text / image / media / archive) | `viewer/` activities and fragments | `compose/features/viewer` | **bridged** | Compose image and text screens exist but are not routed. Archive viewer and save-as flow are the riskiest: keep legacy until parity is proven. |
| Operations / conflicts | `filejob/`, `filelist/` conflict dialogs | `compose/features/operations` | **pending** | `OperationUi` and `ConflictUi` are contracts only; job engine is frozen. |
| Servers (SMB / FTP / SFTP / WebDAV) | `storage/` server editors, providers under `provider/` | `compose/features/settings` (server management); extraction to `compose/features/servers` requires integrator approval | **pending** | UI only. Provider implementations and credentials storage are frozen. |
| FTP server | `ftpserver/FtpServerActivity`, `FtpServerFragment`, service and tile | `compose/features/settings` (FTP section); extraction to `compose/features/ftpserver` requires approval | **pending** | Service, notification, tile and receiver stay legacy; only the control screen moves. |
| About / licenses | `about/AboutActivity`, `AboutFragment` | `compose/features/settings` | **bridged** | A Compose about screen exists but is not routed. Keep GPLv3 and upstream attribution visible; do not move license text behind a web-only link. |

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
