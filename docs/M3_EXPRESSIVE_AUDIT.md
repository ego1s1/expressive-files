# Material 3 Expressive audit and plan

**Snapshot:** 2026-11-05
**Scope:** the Compose-first shell in `app/src/main/java/me/zhanghai/android/files/compose/**`
(56 files, ~9,770 LOC), the shared theme, and the design contract documents.
**Decision record:** Excellent intensity, Material3 1.5.0-rc01, minSdk 24, hybrid
navigation, accessibility first, fully wired theming, screenshot + UI tests.

## Verdict

| Dimension | Before | After this pass | Target |
|---|---:|---:|---:|
| Material 3 baseline | ~75% | ~85% | 90%+ |
| **M3 Expressive** | ~40% | ~60% | 80%+ |
| Theme / color | 60% | 75% | 85% |
| Typography | 55% | 70% | 85% |
| Shape | 40% | 65% | 80% |
| Motion | 35% | 70% | 85% |
| Components | 55% | 60% | 85% |
| Accessibility | 45% | 65% | 90% |
| Adaptive layout | 35% | 35% | 80% |
| Consistency / testing | 35% | 45% | 80% |

The app was a well-structured Material 3 app with a strong written contract, but it was
not yet M3 **Expressive**. The main gaps were motion, shape, font-scale resilience,
truthful settings, and documentation that promised APIs Material3 1.4.0 did not expose.

## Verified API facts

These were checked against the cached artifacts, not assumed from the docs.

| API | 1.4.0 | 1.5.0-rc01 |
|---|---|---|
| `MotionScheme` interface | public | public |
| `MotionScheme.expressive()` | internal (`expressive$material3`) | **public** |
| `MaterialTheme(..., motionScheme, ...)` | internal | public |
| `MaterialExpressiveTheme` | internal | **public** |
| `expressiveLightColorScheme()` | public | public |
| `ButtonGroup` / `LoadingIndicator` | token classes only | **public composables** |
| `FloatingToolbar` / `SplitButton` | tokens only | **public** |
| `MaterialShapes` | absent | **public** |
| `Shapes.largeIncreased` etc. | internal | **public** |

The 1.4.0 limitation was real, which is why the previous fallback existed. The 1.5
upgrade removes the excuse: the theme now uses `MaterialExpressiveTheme` and
`MotionScheme.expressive()` directly.

## Resolved in this pass

| Finding | Resolution |
|---|---|
| Design doc demanded `ButtonGroup` / loading indicator that 1.4.0 did not ship | Upgraded to 1.5.0-rc01; expressive components are now available. |
| No expressive motion scheme | `ExpressiveFilesTheme` uses `MaterialExpressiveTheme` + `MotionScheme.expressive()`. |
| Custom motion specs were off-spec (`0.9/700`) | `ExpressiveMotion` now uses `0.8/380` spatial, `0.8/800` fast, `0.8/200` slow, `1.0/1600` effects. |
| Fixed heights clipped at 200% font scale | File rows, top bars, search fields, sort rows, FAB menu rows and home headers now use `heightIn(min = …)`. |
| List selection was color-only | `FileRow` now shows a check icon and keeps `selected` semantics. |
| No reduced-motion handling | `rememberMotionEnabled()` combines the app switch with the system animator scale; custom transitions snap when disabled. |
| Settings were not truthful | `BLACK_NIGHT_MODE`, `DYNAMIC_COLOR` and `FILE_LIST_ANIMATION` are now observed by the Compose theme. |
| Hardcoded 20sp / 28dp / 16dp / 4dp | Replaced with `titleLarge`, `BottomSheetDefaults.ExpandedShape`, and `MaterialTheme.shapes`. |
| 13 previews used raw `MaterialTheme` | All previews now use `ExpressiveFilesPreview`. |
| Shape scale had no expressive steps | `ExpressiveShapes` now includes 20 / 32 / 48dp extensions. |
| File names used `titleMedium` | File rows use `bodyLarge` with two lines, matching the contract. |

## Open findings (prioritized)

### P0 — accessibility and correctness

1. **Font-scale verification is still manual.** The fixed heights are gone, but the
   matrix (1.3, 2.0, RTL, TalkBack) has not been run. Add Compose UI tests and golden
   images before declaring the surface compliant.
2. **Motion setting granularity.** `FILE_LIST_ANIMATION` currently controls all custom
   Compose motion. Rename or split it into a general “Animations” switch so the label
   matches the behavior.
3. **True black is dark-only.** The setting is ignored in light mode by design; document
   that, and verify contrast of all tonal containers against pure black.

### P1 — expressive completeness

4. **Navigation is still custom.** The floating pill and capsule rail keep their
   identity, but use `clickable(Role.Tab)` + manual `selected` instead of `selectable`,
   20dp icons instead of 24dp, and no standard rail geometry. Hybrid navigation is the
   chosen direction; the semantics and medium-width rail still need work.
5. **App bar is custom.** `FileListTopBar` uses a 64dp minimum bar with three actions.
   Move to the M3 flexible/medium app bar or document the custom bar as intentional.
6. **FAB menu is hand-built.** 1.5 exposes `FloatingActionButtonMenu`; replacing the
   custom `AnimatedVisibility` menu would add correct expansion semantics, overflow
   behavior and expressive motion for free.
7. **Remaining expressive components are unused.** `ButtonGroup`, `LoadingIndicator`,
   `SplitButton` and `MaterialShapes` are available now. Adopt them where they map to
   real actions (create group, long operations, destructive split action).
8. **Adaptive layout is two literals.** `600.dp` and `390.dp` are hardcoded in
   `ExpressiveFilesApp.kt`; there is no expanded two-pane layout.
9. **Translations are still pending.** Every shipping Compose string is now externalized
   (`res/values/strings_compose.xml`, plurals included); new keys need translated
   resources before a locale-expanded release.
10. **Screenshot goldens are still missing.** Compose UI tests now cover selected
    semantics, row clicks and the true-black palette; CI has no visual job yet.

## Plan

| Phase | Work | Status |
|---|---|---|
| **0. Truth-up + guardrails** | Update DESIGN/CONTRACT for 1.5; add static checks for hardcoded colors, `sp`, shapes and springs; require `ExpressiveFilesPreview`. | **done (docs)** / checks pending |
| **1. Accessibility P0** | Remove fixed heights; selection check icon; reduced-motion policy; truthful settings; font-scale pass. | **code done**, verification pending |
| **2. Theme foundation** | Expressive theme + motion scheme; 20/32/48 shapes; true black; dynamic-color setting; token cleanup. | **done** |
| **3. Components + nav** | `selectable` nav semantics, 24dp icons, rail geometry; app bar decision; FAB menu → `FloatingActionButtonMenu`; `ButtonGroup` / `LoadingIndicator`; sheet shape unification. | next |
| **4. Adaptive** | Centralize breakpoints; medium rail; expanded two-pane at ≥840dp; inset/FAB clearance. | pending |
| **5. Quality** | Compose UI tests for selection, row clicks and true black (done); Roborazzi goldens and a CI visual job remain. | **UI tests done**, screenshots pending |
| **6. Localization** | Externalize all shipping Compose strings; add locale translations; retire “Open legacy settings”. | **strings externalized**, translations pending |

### Phase 3 detail

- **Navigation:** keep the phone pill; convert the medium/expanded rail to a proper
  vertical surface with 24dp icons and `selectable(role = Role.Tab)`. Keep labels for
  the selected destination and tooltips for the rest.
- **Top app bar:** prefer `TopAppBar` / `MediumFlexibleTopAppBar`; if the custom bar
  stays, give it a documented reason and a single source of truth for height.
- **FAB:** evaluate `FloatingActionButtonMenu` against the current create menu; keep the
  current menu only if it preserves the file-specific actions and tests.
- **Buttons:** group “New file / New folder” with `ButtonGroup` where the screen has
  room; keep the overflow for paste and destructive actions.
- **Progress:** use `LoadingIndicator` for indeterminate operations longer than a
  moment; keep determinate `LinearProgressIndicator` with the percentage label.

### Phase 4 detail

- Introduce `WindowSizes.kt` with compact/medium/expanded thresholds and use
  `BoxWithConstraints` from one place.
- At ≥840dp show list + preview/properties; keep the rail.
- Verify 360dp, 700dp, 1000dp; portrait and landscape; three-button and gesture nav.

## Acceptance criteria

A screen is M3 Expressive compliant when:

- [ ] All colors, type, shape and motion values come from `MaterialTheme`.
- [ ] It survives 200% font scale, RTL, dark, true black and dynamic color.
- [ ] Selection, error and disabled states are understandable without color.
- [ ] Every icon-only control has a label and a 48dp target.
- [ ] Motion is meaningful, interruptible, and snaps when animations are disabled.
- [ ] It renders at compact, medium and expanded widths.
- [ ] It has a light/dark preview using `ExpressiveFilesPreview`.
- [ ] It has a screenshot golden and at least one interaction test if it owns state.

## Risks

- **1.5.0-rc01 is a release candidate.** Pin it, keep the BOM for Compose UI/runtime,
  and re-check the API surface before every upgrade. Deprecations already surfaced:
  `rememberModalBottomSheetState`, `ListItem` overload, `Icons.Filled.ArrowBack`,
  `Icons.Filled.KeyboardArrowRight`.
- **MaterialExpressiveTheme affects built-in motion globally.** Verify dialogs and
  sheets do not feel sluggish; the effects spec must stay critically damped.
- **True black can hide tonal hierarchy.** Test containers against pure black; do not
  use black for cards that need separation.
- **Dynamic color can break brand assumptions.** Keep the seeded fallback palette
  reviewed and provide a dynamic-color toggle (now wired).
- **Compose 1.12.1 + Material3 1.5.0-rc01** is a mixed-version pair. Compile and
  smoke-test before release; do not upgrade both blindly.
- **minSdk moved from 23 to 24.** Material3 1.5 requires it. Android 5.0 is no longer
  supported; README, the test matrix and release notes must say Android 7.0+.

## Test matrix to run next

| Dimension | Values |
|---|---|
| Font scale | 1.0, 1.3, 2.0 |
| Theme | Light, dark, true black, dynamic on/off |
| Width | 360dp, 700dp, 1000dp |
| Locale | English, German, Arabic (RTL), Hebrew (RTL) |
| Motion | Animations on, animations off |
| Input | Touch, keyboard, TalkBack |
| API | 23, 29, 33, 35, 36 |
