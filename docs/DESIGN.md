# Expressive Files — design system

This document is the visual contract for the Compose-first fork. It maps Material 3
Expressive intent onto the tokens available in **Material3 1.5.0-rc01** and the shared
[`COMPOSE_CONTRACT.md`](COMPOSE_CONTRACT.md). Material3 1.5 is pinned deliberately so
`MaterialExpressiveTheme`, `MotionScheme.expressive()`, `ButtonGroup`,
`LoadingIndicator`, `FloatingActionButtonMenu`, `SplitButton` and `MaterialShapes` are
available. Screens must consume the theme through `MaterialTheme`; this file is the
source of truth when a component spec is ambiguous.

The one rule that outranks everything else: **tokens, not literals**. No screen may
hardcode a hex color, `sp` size, corner radius, elevation, or spring. If a token is
missing, add it to the theme package — not to the feature package.

## Token architecture

```
ExpressiveFilesTheme
├── colorScheme   MaterialTheme.colorScheme (light / dark; true black is a future variant)
├── typography    MaterialTheme.typography (M3 type scale + emphasized weights)
├── shapes        MaterialTheme.shapes (M3 shape scale, expressive extensions)
└── motionScheme  MaterialExpressiveTheme + MotionScheme.expressive()
```

Every screen is stateless and receives `state` plus callbacks. Colors, type, shape and
motion are therefore theme concerns, not screen concerns. Previews must use the same
theme wrapper so a preview cannot silently diverge from the app.

## Color roles

Expressive Files uses tonal surfaces rather than shadows. Cards, sheets, app bars and
selected rows are separated by surface-container steps and, only where necessary, by
`outlineVariant` dividers.

| Role | Token | Use in the fork | Avoid |
|---|---|---|---|
| Canvas | `surface` | File list background, viewer background | Pure white/black literals |
| Low chrome | `surfaceContainerLowest` / `surfaceContainerLow` | Recessed panels, search results background | Large text areas with low contrast |
| App bar / selection bar | `surfaceContainer` | Top app bar, bottom bar, selection bar | `primary` as a full-bleed bar |
| Card / sheet | `surfaceContainerHigh` | Cards, dialogs, bottom sheets, search bar | Stacking many elevations |
| Highest chrome | `surfaceContainerHighest` | Menus, tooltips, drag handles, pressed sheets | Body backgrounds |
| Accent | `primary` / `onPrimary` | FAB, primary buttons, active progress, links | Body text |
| Tonal selection | `secondaryContainer` / `onSecondaryContainer` | Selected file rows/tiles, selected chips | Destructive states |
| Supporting accent | `tertiary` / `tertiaryContainer` | Optional badges, storage-type accents | Competing with primary actions |
| Primary text | `onSurface` | File names, values, titles | Low-emphasis metadata |
| Secondary text | `onSurfaceVariant` | Sizes, dates, paths, summaries | Primary actions |
| Divider | `outlineVariant` | 1dp dividers, grid gutters | Borders on every card |
| Outline | `outline` | Text-field borders, unselected controls | Text |
| Destructive | `error` / `onError` | Delete, overwrite, failed operations | Non-destructive emphasis |
| Destructive container | `errorContainer` / `onErrorContainer` | Error banners, conflict cards | Buttons that are not destructive |
| Inverse | `inverseSurface` / `inverseOnSurface` | Snackbars | Toasts (floating snackbars are preferred) |
| Scrim | `scrim` | Modal sheet/dialog scrim | Opaque blockers |
| Dim / bright | `surfaceDim` / `surfaceBright` | Optional large-screen depth | As a replacement for layout |

Rules:

- Selection is communicated by **container + check icon + state description**, never by
  color alone.
- Disabled content uses the theme's disabled alpha on `onSurface`; do not invent a grey.
- File-type identity comes from the icon/thumbnail, not from a per-extension color.
- Light and dark must be checked. If the optional true-black variant is implemented,
  check it as well. A color that only works on `surface` is not a token choice, it is a
  bug.
- Dynamic color is on by default on Android 12+ when the theme enables it. It may change
  every role above; never assume a fixed hue.

## Typography

Use `MaterialTheme.typography.*` only. The table records the default M3 scale so
reviewers can reason about hierarchy; feature code still references the token.

| Token | Default size / line height | Fork usage |
|---|---|---|
| `displaySmall` | 36 / 44 | Empty-state hero numerals only (rare) |
| `headlineSmall` | 24 / 32 | Dialog titles, large empty states |
| `titleLarge` | 22 / 28 | Top app bar title, screen title |
| `titleMedium` | 16 / 24 | Section headers, prominent row labels |
| `titleSmall` | 14 / 20 | Dense section labels, settings group headers |
| `bodyLarge` | 16 / 24 | File names, primary row text, property values |
| `bodyMedium` | 14 / 20 | Supporting text, paths, summaries, dialog body |
| `bodySmall` | 12 / 16 | Timestamps, legal text, dense metadata |
| `labelLarge` | 14 / 20 | Buttons, chips, navigation labels |
| `labelMedium` | 12 / 16 | Badges, counters, overline labels |
| `labelSmall` | 11 / 16 | Compact badges only |

Expressive emphasis:

- Emphasis comes from weight and placement, not from new font families. The theme
  already applies `FontWeight.SemiBold` to headline and title roles; do not add another
  weight on top. In body content, use `FontWeight.Medium` only for the one label that
  must lead.
- File names use `bodyLarge` with `maxLines = 2` and `TextOverflow.Ellipsis`; never
  shrink text to fit. Rows grow with font scale instead of clipping.
- Use tabular figures for counts, sizes and progress percentages when available.
- Do not use all caps for file names; `labelMedium` may use it for short section
  overlines if the translation is safe.
- Long paths are truncated from the start where the text API supports it, or shown in a
  scrollable single line; the file name is never sacrificed for the path.

## Shape

| Token | Radius | Fork usage |
|---|---|---|
| `extraSmall` | 4dp | Badges, counters, tiny status dots |
| `small` | 8dp | Chips, text fields, thumbnails, small buttons |
| `medium` | 12dp | Grid tiles, list-row leading thumbnails, menu items |
| `large` | 16dp | Cards, grouped settings sections, banners |
| `extraLarge` | 28dp | Dialogs, bottom sheets (top corners), large cards |
| `largeIncreased` | 20dp | Expressive large cards, grouped actions |
| `extraLargeIncreased` | 32dp | Floating toolbars, hero sheets |
| `extraExtraLarge` | 48dp | Full-screen dialogs and immersive surfaces |

Rules:

- The current theme implements the five baseline tokens above. Do not assume extended
  shapes exist, and do not use raw `RoundedCornerShape` in a feature.
- Interactive surfaces use `shapes.*`, not `RoundedCornerShape(12.dp)`.
- A selected grid tile keeps its shape and changes its container; it does not jump to a
  different silhouette.
- Full-width bottom sheets use `extraLarge` on the top corners only.
- Menus and dialogs do not mix shape families in one surface.

## Motion

M3 Expressive separates **spatial** motion (position, size, bounds, scale) from
**effects** motion (color, alpha, tonal elevation). Material3 1.4.0 does not expose an
expressive public `MotionScheme` in 1.4.0, so the project upgraded to Material3 1.5,
where `MotionScheme.expressive()` is public. `MaterialExpressiveTheme` installs it for
built-in components; `compose/theme/Motion.kt` re-exports the same specs for custom
animations.

| Token | Spring | Use |
|---|---|---|
| `ExpressiveMotion.spatial()` | damping `0.8`, stiffness `380` | Row expansion, sheet position, FAB morph, shape changes |
| `ExpressiveMotion.spatialFast()` | damping `0.8`, stiffness `800` | Small, immediate state changes |
| `ExpressiveMotion.spatialSlow()` | damping `0.8`, stiffness `200` | Large surfaces such as sheets |
| `ExpressiveMotion.effects()` | damping `1.0`, stiffness `1600` | Container color, alpha, tonal elevation |

Do not hand-roll duration specs in a feature package. When `rememberMotionEnabled()` is
false (system animator scale 0 or the app animation switch off), every custom transition
must snap to the final state.

Rules:

- Motion explains a state change; it is never decoration. File list scrolling has no
  entrance animations.
- Spatial springs may overshoot slightly; effects must not. Color and opacity use
  critically damped specs.
- Respect the system animator duration scale. With animations disabled, every
  transition still lands in a correct final state and no information is lost.
- Do not animate progress values backwards, and do not loop motion behind readable
  content except for indeterminate progress.
- Destructive confirmations use short effects motion; never a playful bounce.

## Spacing and layout metrics

Spacing is a 4dp grid: **4, 8, 12, 16, 24, 32**.

| Metric | Value | Notes |
|---|---|---|
| Screen horizontal padding | 16dp | 24dp on expanded width |
| Icon-to-label gap | 8dp | 12dp for leading file thumbnails |
| List row height | 72dp minimum | Grows with font scale; never clips |
| Grid tile | 1:1 | Minimum 2 columns; adaptive up to 6+ |
| Top app bar | 64dp | Plus status-bar inset |
| Bottom bar | 64dp | Plus navigation-bar inset |
| Chip height | 32dp | Touch target still 48dp |
| FAB | 56dp | Extended FAB 56dp high; 16dp from safe edges |
| Touch target | 48dp minimum | Even when the visual is 32dp or 40dp |
| Divider | 1dp | `outlineVariant` |

Adaptive rules:

- Compact (< 600dp): one pane, drawer navigation, bottom actions.
- Medium (600–839dp): navigation rail, list stays single column.
- Expanded (≥ 840dp): list + preview/properties two-pane layout; dialogs may become
  side sheets only if the interaction remains modal.
- Insets are consumed with `WindowInsets`, never with hardcoded status-bar heights.

## Component rules

### Top app bar and selection

- Container `surfaceContainer`; title `titleLarge`; 64dp content height.
- Normal mode: navigation icon, title, up to three actions, overflow.
- Selection mode: close/back icon, `"N selected"` title, select-all and overflow.
  Hide the FAB and bottom navigation while selecting.
- Every icon-only action has a `contentDescription`; the overflow is always reachable
  by keyboard and TalkBack.
- Long titles truncate with ellipsis; do not wrap the app bar title.

### File list / grid

- Rows: 72dp minimum, leading 40dp icon or thumbnail with `shapes.small`, name
  `bodyLarge`, supporting text `bodyMedium`/`onSurfaceVariant`, trailing overflow.
- Selected row: `secondaryContainer` container, `onSecondaryContainer` content, check
  icon; preserve icon legibility.
- Grid: 1:1 tile, thumbnail centered, name below, two lines maximum, selected tile
  gets a tonal container and check badge.
- Directories and files share alignment. Symlink, hidden and archive state is conveyed
  by icon + badge text, not by color alone.
- Loading, empty and error states replace the list body without moving the app bar.
  Empty state uses `titleLarge` + `bodyMedium` and at most one primary action.
- Search results use the same row contract; highlight matches with weight/tonal
  background, not a new color role.

### Navigation and drawer

- Drawer width 320dp; header uses `surfaceContainerHigh`; entries are 48dp minimum.
- Current destination uses `secondaryContainer` + `onSecondaryContainer`; the selected
  indicator spans the full row, not just the icon.
- Drawer actions (settings, FTP, about) are visually distinct from destinations when
  they leave the navigation graph.
- Home sections use `titleMedium` headers and `bodyMedium` supporting text; recents and
  favorites are lists, not card carousels.

### Settings

- Grouped lists on `surface`; section headers `titleSmall`/`primary`; rows 72dp.
- Control type must match the value: switch for booleans, radio/choice for one-of-many,
  slider for bounded numbers, action row for navigation.
- Summary text uses `bodyMedium`/`onSurfaceVariant`; a value label is end-aligned and
  truncates before the control.
- Destructive settings (clear cache, remove server, delete bookmark) ask for
  confirmation and use `error` for the confirming action only.

### Properties

- Header: icon, file name `titleLarge`, path `bodyMedium` with start ellipsis.
- Tabs use `primary` indicator and `labelLarge`; do not scroll-hide tab labels.
- Label/value rows: label `bodyMedium`/`onSurfaceVariant`, value `bodyMedium`/
  `onSurface`, copy action with `contentDescription`.
- Long values wrap; monospace is allowed only inside a value token if the theme defines
  it, never as a new font family in a feature package.

### Viewers

- Content-first: the viewer uses the theme's media surface (`surface` for documents,
  `scrim` behind fullscreen media); chrome is overlaid with a tonal scrim.
- Controls auto-hide only for immersive media; a single tap restores them and does not
  trigger a second action.
- Text viewer uses `bodyLarge`, respects font scale, supports select/copy, and keeps
  line numbers in `bodySmall`/`onSurfaceVariant`.
- Media controls have 48dp targets, content descriptions, and a visible focus state.

### Operations, conflicts and servers

- Progress: track `secondaryContainer`, indicator `primary`; determinate operations show
  a percentage label. Cancellation stays available and is never the primary-styled
  button.
- Conflict dialogs show the existing and incoming names, allow "apply to all" only when
  the operation supports it, and make the destructive choice explicit.
- Server forms (SMB/FTP/SFTP/WebDAV) use outlined fields with `shapes.small`, inline
  errors with icon + text, and status chips that never rely on color alone.
- FTP server state is a status, not a color: show "Running"/"Stopped" text plus icon.
- Error banners use `errorContainer`; retry is a text button, not a floating FAB.

### Dialogs, sheets and snackbars

- Dialog container `surfaceContainerHigh`, `extraLarge` shape, title `headlineSmall`,
  body `bodyMedium`, actions end-aligned text buttons.
- Bottom sheet: `extraLarge` top corners, drag handle, `surfaceContainerHigh`; modal
  sheets dismiss on scrim tap only when no unsaved input exists.
- Snackbar: `inverseSurface`/`inverseOnSurface`, one action, 4dp/8dp spacing, and it
  must not cover the FAB or bottom bar (use insets).
- Menus: `surfaceContainerHighest`, `small` or `medium` shape, 48dp items.

### Expressive components and constraints

- Use `ButtonGroup` for two to four closely related actions (for example create file /
  create folder). More actions belong in overflow.
- Use the Material3 loading indicator for operations longer than a moment, always with
  a text label; never replace the file name with a spinner.
- Shape morphing belongs to the component's state transition (for example FAB to
  extended FAB). Do not morph file rows or grid tiles on scroll.
- Material3 1.5.0-rc01 is the pinned ceiling. Prefer the first-party expressive
  components (`FloatingActionButtonMenu`, `ButtonGroup`, `LoadingIndicator`,
  `SplitButton`, `FloatingToolbar`) over bespoke equivalents. Keep the custom create
  menu only if it preserves the file-specific actions and has UI tests.
- Expressive personality is allowed in motion, shape and type weight. It is not
  allowed in destructive confirmations, permission prompts or error recovery.

## Do / Don't

**Do**

- Use `MaterialTheme.colorScheme`, `.typography`, `.shapes` and the theme motion specs.
- Keep screens stateless; preview every main screen in light and dark.
- Use `start`/`end` padding and mirror directional icons for RTL.
- Give every icon-only control a `contentDescription` and a 48dp touch target.
- Test at 200% font scale, in Arabic/Hebrew, and with TalkBack.
- Use tonal surfaces and spacing for hierarchy before reaching for elevation.
- Keep destructive actions visually quiet until the confirmation step.
- Prefer one primary action per screen; move secondary actions into overflow.

**Don't**

- Hardcode colors, `sp` sizes, corner radii, elevations, or spring values.
- Use Material 2 components or unapproved Material3 alpha APIs (see the Compose contract).
- Encode selection, status, or errors with color alone.
- Shrink or clip text to preserve a fixed row height.
- Animate the file list on scroll or loop motion behind content.
- Put a FAB over a list row's trailing action or over the bottom bar.
- Use full-bleed `primary` app bars, gradients, glassmorphism, or per-file accent colors.
- Rewrite legacy layouts, themes or drawables as part of a Compose screen change.

## Review checklist

1. Does every color/type/shape/motion value come from `MaterialTheme`?
2. Are selection, error and disabled states understandable without color?
3. Do all interactive elements meet 48dp and expose a label?
4. Does the screen survive 200% font scale, RTL, dark theme (and true black if the
   variant exists)?
5. Is the layout correct at compact, medium and expanded widths?
6. Is motion meaningful, interruptible, and harmless when animations are disabled?
7. Does the screen remain usable with TalkBack and keyboard/switch access?
