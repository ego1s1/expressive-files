# Release runbook

**Target:** Expressive Files 1.0.0 (versionCode 1), minSdk 24, targetSdk 36,
compileSdk 37.

## Prerequisites

- JDK 21 (`JAVA_HOME` must point to it)
- Android SDK 37, build-tools 37.0.0, NDK 27.2.12479018
- Initialized submodules
- Material3 is pinned to `1.5.0-rc01`; do not rely on the Compose BOM for it

## Versioning

`versionCode` and `versionName` live in `app/build.gradle`. Every published build
must increment `versionCode`. The fastlane changelog is
`fastlane/metadata/android/en-US/changelogs/<versionCode>.txt`.

## Signing

Never commit keystores or passwords. The release config reads, in order:

1. `signing.properties` in the repository root (gitignored)
2. Environment variables `STORE_FILE`, `STORE_PASSWORD`, `KEY_ALIAS`,
   `KEY_PASSWORD`
3. An interactive prompt

Example `signing.properties`:

```properties
storeFile=/absolute/path/to/release.jks
storePassword=...
keyAlias=...
keyPassword=...
```

CI should provide the same values as encrypted environment secrets and must not
print them.

## Build and verify

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)

# Fast feedback
./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug

# Release APK
./gradlew :app:assembleRelease

# Play Store bundle
./gradlew :app:bundleRelease
```

Artifacts:

- `app/build/outputs/apk/debug/app-debug.apk`
- `app/build/outputs/apk/release/app-release.apk`
- `app/build/outputs/bundle/release/app-release.aab`

Verify the release signature before publishing:

```bash
$ANDROID_HOME/build-tools/37.0.0/apksigner verify --print-certs \
  app/build/outputs/apk/release/app-release.apk
```

The certificate must match the Play Console upload key. A debug or self-signed
test key is acceptable only for local smoke builds.

## Release checklist

- [ ] `:app:testDebugUnitTest` passes.
- [ ] `:app:lintDebug` passes with no errors.
- [ ] `:app:assembleRelease` and `:app:bundleRelease` pass.
- [ ] Release APK installs and launches on API 24 and API 36.
- [ ] Light, dark, true black and dynamic color checked.
- [ ] 200% font scale, RTL and TalkBack smoke test.
- [ ] Version code and fastlane changelog match.
- [ ] `README.md` and `PRIVACY.md` are current.
- [ ] The GPLv3 attribution to Material Files remains visible in About/licenses.

## Verification record

Local verification on 2026-11-06:

- `:app:testDebugUnitTest` — 26 tests, 0 failures.
- `:app:lintDebug` — 0 errors.
- `:app:assembleDebug` — passed.
- `:app:assembleRelease` — passed, V2-signed test APK verified with `apksigner`.
- `:app:bundleRelease` — passed.
- Release APK installs and launches on emulator `sdk_gphone64_arm64`, API 35.
- Files, Favorites, Settings, selection, FAB menu, true black, 200% font scale and
  medium rail were smoke-tested.
- Release APK installs and launches on API 24 (`expressive-api24`, Android 7.0).
- Arabic RTL and German long-label runs passed; the accessibility tree reports
  29/29 clickable nodes with text or content descriptions.
- Expanded two-pane layout verified at 1920x1200 (1920dp wide): selection shows
  properties in the right pane.
- Store phone screenshots were replaced with 1080x1920 captures; tablet screenshots
  were captured at 1920x1200.
- All supported locales now have draft Compose translations; native review is still
  recommended.
- Expanded two-pane layout and selected-row properties verified at 1920x1200.
- File-row screenshot goldens run in `:app:testDebugUnitTest`.

The local test key is not a store key. Replace it with the Play upload key before
publishing.

## Known release constraints

- Material3 `1.5.0-rc01` is a release candidate. Pin it, review the diff on each
  upgrade, and keep the Compose BOM at 2026.09.00 until the next coordinated bump.
- All shipping Compose strings are externalized in
  `app/src/main/res/values/strings_compose.xml`. Locale resources for the new keys are
  still being translated; English is the fallback.
