# Trust Core Module (`:trust-core`)

> **Stretch Goal Implementation**: Gradle module split with `:trust-core` (pure Kotlin library) and `:app` (Android UI).

## Overview
`:trust-core` is a pure Kotlin JVM library module containing zero Android SDK dependencies. It encapsulates the core trust domain models, interfaces, state machine definitions, and security contracts.

## Explicit Dependency Direction
- **`:trust-core`**: Pure Kotlin, zero Android dependencies.
- **`:app`**: Depends on `:trust-core` (`implementation(project(":trust-core"))`), providing Android implementations (`EncryptedSharedPreferences`, `SignalCollectorImpl`, Retrofit API client, Jetpack Compose UI, and Koin UI bindings).
