# System Optimizer

Aplikasi Android System Optimizer yang powerful, ringan, dan stabil. Dioptimasi untuk perangkat Realme 3 (4GB/64GB).

## Fitur
- **RAM Optimizer** - Bebaskan memori dan tingkatkan performa
- **Cache Cleaner** - Bersihkan cache dan file sampah
- **Battery Saver** - Optimasi penggunaan baterai
- **Process Manager** - Kill background processes

## Tech Stack
- Kotlin + Jetpack Compose
- Material Design 3
- MVVM + Clean Architecture
- Hilt Dependency Injection
- Coroutines + Flow

## Build
```bash
./gradlew assembleDebug
```

## CI/CD
Build otomatis via GitHub Actions:
- Push ke `main` → Debug + Release APK
- Push ke `develop` → Debug APK
- Download artifact dari tab Actions
