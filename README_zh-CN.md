# Expressive Files

[English](README.md)

一个适用于 Android 7.0+ 的开源文件管理器，使用 Material 3 Expressive 构建。

[![Android CI](https://github.com/ego1s1/expressive-files/actions/workflows/android.yml/badge.svg)](https://github.com/ego1s1/expressive-files/actions/workflows/android.yml)
[![License: GPLv3](https://img.shields.io/badge/license-GPLv3-blue.svg)](LICENSE)

Expressive Files 是 [Material Files](https://github.com/zhanghai/MaterialFiles)（作者
Hai Zhang）的 Compose 优先分支。它保留了经过验证的 Linux 文件后端，并将界面替换为
Material 3 Expressive：动态取色、表现力动效、纯黑主题、自适应导航和无障碍控件。

## 功能

- 开源、轻量且安全。
- Material 3 Expressive：动态取色、表现力动效和 20/32/48dp 形状。
- 面包屑、列表/网格、搜索、排序和选择。
- Root、压缩包以及 FTP/SFTP/SMB/WebDAV 服务器。
- 纯黑、动态取色和动画设置。
- 支持符号链接、权限和 SELinux 上下文。
- 基于 Java NIO2 File API 和稳定的文件任务引擎。

## 构建

需要 JDK 21、Android SDK 37、build-tools 37.0.0、NDK 27.2.12479018，并初始化子模块。

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
```

发布签名和商店步骤见 [`docs/RELEASE.md`](docs/RELEASE.md)。

## 文档

- [`docs/M3_EXPRESSIVE_AUDIT.md`](docs/M3_EXPRESSIVE_AUDIT.md)
- [`docs/DESIGN.md`](docs/DESIGN.md)
- [`docs/COMPOSE_CONTRACT.md`](docs/COMPOSE_CONTRACT.md)
- [`docs/MIGRATION.md`](docs/MIGRATION.md)
- [`docs/TESTING.md`](docs/TESTING.md)

## 许可证

GPLv3，详见 [`LICENSE`](LICENSE)。本项目基于 Hai Zhang 的 Material Files，并保留原始
版权与 GPLv3 署名。
