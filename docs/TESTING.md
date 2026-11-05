# Testing Expressive Files

The fork has JVM unit tests, Robolectric-backed Compose UI tests, and file-row
screenshot goldens. Broader screen goldens are still planned; this document defines
how to run what exists and the manual matrix that must pass before a Compose surface
is called migrated.

## Prerequisites

- JDK 21 (`JAVA_HOME` points to a JDK 21 installation)
- Android SDK 37 with build-tools 37.0.0 and NDK 27.2.12479018
- Git submodules initialized (`git submodule update --init --recursive`)

The project uses Gradle 9.6 and AGP 9.4.1. Do not downgrade the JDK to satisfy an old
toolchain; the build targets Java 11 bytecode with core library desugaring.

## Run unit tests

```bash
# macOS
JAVA_HOME=$(/usr/libexec/java_home -v 21) ./gradlew :app:testDebugUnitTest

# Linux / CI
JAVA_HOME=/usr/lib/jvm/temurin-21-jdk-amd64 ./gradlew :app:testDebugUnitTest
```

Run a single class or test while iterating:

```bash
JAVA_HOME=$(/usr/libexec/java_home -v 21) ./gradlew :app:testDebugUnitTest \
  --tests "me.zhanghai.android.files.compose.UiModelsTest"

JAVA_HOME=$(/usr/libexec/java_home -v 21) ./gradlew :app:testDebugUnitTest \
  --tests "me.zhanghai.android.files.compose.UiModelsTest.fileLayoutMode_declaresListThenGrid"
```

Reports:

- HTML: `app/build/reports/tests/testDebugUnitTest/index.html`
- XML: `app/build/test-results/testDebugUnitTest/`
- Re-run only failed tests: add `--rerun-tasks` or delete the task output; Gradle marks
  the task up-to-date otherwise.

The debug APK is built with:

```bash
JAVA_HOME=$(/usr/libexec/java_home -v 21) ./gradlew :app:assembleDebug
```

Artifact: `app/build/outputs/apk/debug/app-debug.apk`.

## What the current unit tests cover

`UiModelsTest` covers the shared stateless models in `compose/model/UiModels.kt`:

- `FileEntryUi` default values and `copy()` field preservation.
- `FileListUiState` empty-directory defaults, selection count consistency
  (`selectedCount >= 0` and matching selected entries for constructed states), and
  chrome preservation across copies.
- `FileLayoutMode` values, ordering, `valueOf`, and layout switching through `copy()`.

The models do not validate input. Tests must not assert clamping of negative
`selectedCount` or other impossible states unless validation is added to the model
first. When a model gains behavior, add a test in the same package and keep test data
explicit.

`FileRowTest` and `ThemeComposeTest` run Compose under Robolectric and cover selected
semantics, row clicks, true-black surfaces and the standard dark palette. Intents,
real file IO and device-only behavior still require instrumentation or manual
verification.

## CI

`.github/workflows/android.yml` runs two independent jobs on every push and pull
request, both with JDK 21:

| Job | Command | Blocking? |
|---|---|---|
| Unit tests, lint and debug build | `./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug` | Yes |
| Lint | `./gradlew :app:lintDebug` | Yes; report is uploaded as an artifact |

Artifacts: `app-debug-apk`, `unit-test-reports`, `lint-report` (10-day retention).

## Golden tests

`FileRowGoldenTest` renders the unselected and selected row under Robolectric native
graphics and compares a 32×32 color signature against
`app/src/test/resources/goldens/file_row_*.png`. It runs in the normal
`:app:testDebugUnitTest` job, so a theme, layout or selection regression fails CI.

To update a golden intentionally, render the row, replace the matching baseline in
`app/src/test/resources/goldens/`, and review the PNG diff in the PR.

## Screenshot matrix (broader coverage to add)

Broader screen goldens are still planned. The intended choice is a JVM renderer
(Paparazzi or Roborazzi), so screenshots render in JVM test tasks without an emulator.
Wire it into CI as a separate, non-blocking job first.

### Smoke set (must pass for every UI PR)

Until the renderer lands, treat this as a manual screenshot review checklist. Once
goldens exist, it becomes the gating golden set.

| Screen / state | Light | Dark | True black* |
|---|---|---|---|
|---|---|---|---|
| File list — populated list | ☐ | ☐ | ☐ |
| File list — grid | ☐ | ☐ | ☐ |
| File list — selection active | ☐ | ☐ | ☐ |
| File list — empty / loading / error | ☐ | ☐ | ☐ |
| File list — search active | ☐ | ☐ | ☐ |
| Drawer / navigation + home | ☐ | ☐ | ☐ |
| Settings | ☐ | ☐ | ☐ |
| Properties (tabs + long values) | ☐ | ☐ | ☐ |
| Viewer (text + media chrome) | ☐ | ☐ | ☐ |
| Operations + conflict dialog | ☐ | ☐ | ☐ |
| Servers + FTP status | ☐ | ☐ | ☐ |
| About / licenses | ☐ | ☐ | ☐ |

\* True black only applies when a true-black theme variant is available. The current
Compose theme exposes light and dark; add the column to the gating set once the
variant lands.

### Extended set (nightly or release-candidate)

| Dimension | Values |
|---|---|
| Font scale | 1.0, 1.3, 2.0 |
| Locale / direction | English, German (long labels), Arabic (RTL), Hebrew (RTL), Japanese, Korean |
| Width | Compact 360dp, medium 700dp, expanded 1000dp |
| Orientation | Portrait, landscape |
| Window | Edge-to-edge with gesture nav, three-button nav, display cutout |
| Input | Touch, keyboard focus visible, TalkBack focus highlight |
| Motion | Animations on, animations off (animator duration scale 0) |
| Density | mdpi (legacy API 24 device), xxxhdpi |
| Theme | Light, dark, dynamic color on/off; true black if available |

File naming convention for future golden images:

```
<screen>__<state>__<theme>__<fontScale>__<width>__<locale>.png
e.g. filelist__selection__dark__1.0__360__en.png
```

Keep golden images deterministic: fixed clock, fixed fixture names, fixed sort order,
no network images, no randomized thumbnails. Store fixtures next to the test source,
not in `app/src/main`.

## Manual accessibility checklist

Run on a physical device or emulator with TalkBack and Switch Access. Record
pass/fail and attach a screen recording for navigation changes.

### Semantics and labels

- [ ] Every icon-only control has a meaningful `contentDescription` (not "button").
- [ ] Decorative icons are excluded from semantics.
- [ ] File rows announce name, type, size/date and selection state in one focus stop.
- [ ] Selected count is announced when selection mode opens and when it changes.
- [ ] Progress and loading states are announced without moving focus unexpectedly.
- [ ] Error and empty states are reachable and their retry action is announced.
- [ ] Custom actions (rename, delete, share, properties) are available from the
      TalkBack actions menu, not only from an overflow icon.
- [ ] Heading semantics are applied to section titles where the screen is long.
- [ ] Focus order follows visual order; the app bar is focused before content.
- [ ] Dialogs and sheets trap focus, restore it on dismiss, and have a dismiss action.

### Touch and contrast

- [ ] All interactive targets are at least 48×48dp, including chips and icon buttons.
- [ ] Text contrast is at least 4.5:1; icons and controls at least 3:1.
- [ ] Selection, error and disabled states are distinguishable without color.
- [ ] Focus indicators are visible in light and dark (and true black if available).
- [ ] No control is placed under the gesture-navigation inset or a display cutout.

### Keyboard and switch access

- [ ] Tab/arrow traversal reaches every action without a touch-only step.
- [ ] Enter/Space activates the focused control; Escape/Back dismisses overlays.
- [ ] Long-press-only actions have a keyboard/switch equivalent.
- [ ] Focus does not get lost after a list update, sort or search refresh.

## Manual RTL checklist

Use Arabic or Hebrew, then force RTL layout direction for an English build as a second
pass.

- [ ] Drawer opens from the right; navigation icons mirror where direction matters.
- [ ] Start/end padding is used throughout; no left/right literals in screen layouts.
- [ ] Breadcrumbs flow RTL and the current directory remains reachable.
- [ ] File names and paths with mixed Latin/Arabic text render with correct bidi order.
- [ ] Back/forward and disclosure chevrons mirror; file-type and media icons do not.
- [ ] Progress indicators, sliders and switches are not mirrored incorrectly.
- [ ] Dialogs and snackbars align actions to start/end as specified.

## Manual font-scale checklist

Test at 1.0, 1.3 and 2.0 (Settings → Display → Font size; also Developer options →
Display cutout/force resize where relevant).

- [ ] App bar titles truncate instead of wrapping over actions.
- [ ] File rows grow vertically; no name, path or badge is clipped.
- [ ] Grid tiles keep a 1:1 media area and allow the label to take extra lines.
- [ ] Settings rows wrap summaries; switches remain vertically centered and reachable.
- [ ] Dialog buttons wrap to a second row when needed; no text is cut off.
- [ ] Property values wrap; copy buttons remain 48dp and do not overlap text.
- [ ] Chips and badges either grow or truncate with an accessible full label.
- [ ] No text uses a fixed `height` that clips ascenders/descenders at 2.0.

## Manual device matrix before a release

| API | Why |
|---|---|
| 24 | Minimum SDK; legacy permission and rendering paths |
| 29 | Scoped storage transition |
| 33 | Notification permission (FTP foreground service) |
| 35 | Edge-to-edge enforcement |
| 36 | Current target SDK behavior |

At minimum run one device per API level plus one tablet/foldable in expanded width.
Record the build SHA, device model, API level, font scale, locale and theme in the test
record.

## Reporting a failure

Include:

1. Build SHA and APK artifact name.
2. Device/emulator, API level, locale, font scale, theme and display size.
3. Screen, state and exact steps.
4. Expected vs actual, with a screenshot or recording.
5. Whether the legacy path still reproduces the issue (for bridged surfaces).
