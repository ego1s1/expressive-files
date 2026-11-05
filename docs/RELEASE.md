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

## Known release constraints

- Material3 `1.5.0-rc01` is a release candidate. Pin it, review the diff on each
  upgrade, and keep the Compose BOM at 2026.09.00 until the next coordinated bump.
- All shipping Compose strings are externalized in
  `app/src/main/res/values/strings_compose.xml`. Locale resources for the new keys are
  still being translated; English is the fallback.
