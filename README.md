# Expressive Files

An open source file manager for Android 7.0+ built with Material 3 Expressive.

[![Android CI](https://github.com/ego1s1/expressive-files/actions/workflows/android.yml/badge.svg)](https://github.com/ego1s1/expressive-files/actions/workflows/android.yml)
[![License: GPLv3](https://img.shields.io/badge/license-GPLv3-blue.svg)](LICENSE)

Expressive Files is a Compose-first fork of [Material Files](https://github.com/zhanghai/MaterialFiles)
by Hai Zhang. It keeps the proven Linux-aware file backend and replaces the UI with a
Material 3 Expressive shell: dynamic color, expressive motion, true black, adaptive
navigation and accessible controls.

## Features

- Open source, lightweight and secure.
- Material 3 Expressive: dynamic color, expressive motion and 20/32/48dp shape steps.
- File browsing with breadcrumbs, list/grid layouts, search, sort and selection.
- Root support, archives, and FTP/SFTP/SMB/WebDAV servers.
- True black, dynamic color and animation settings.
- Linux-aware: symbolic links, permissions and SELinux context.
- Java NIO2 File API and a frozen, well-tested job engine.

## Build

Requirements:

- JDK 21
- Android SDK 37 with build-tools 37.0.0 and NDK 27.2.12479018
- Initialized submodules

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
```

Release signing, verification and store steps are in
[`docs/RELEASE.md`](docs/RELEASE.md).

## Documentation

- [`docs/M3_EXPRESSIVE_AUDIT.md`](docs/M3_EXPRESSIVE_AUDIT.md) — audit, decisions and
  remaining plan.
- [`docs/DESIGN.md`](docs/DESIGN.md) — visual and token contract.
- [`docs/COMPOSE_CONTRACT.md`](docs/COMPOSE_CONTRACT.md) — Compose rules and stack.
- [`docs/MIGRATION.md`](docs/MIGRATION.md) — legacy/Compose surface tracker.
- [`docs/TESTING.md`](docs/TESTING.md) — test and manual QA matrix.

## License

GPLv3. See [`LICENSE`](LICENSE). Expressive Files is based on Material Files by
Hai Zhang; the original copyright and GPLv3 attribution are preserved in the About
and licenses screens.
