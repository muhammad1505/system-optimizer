# System Optimizer

Aplikasi Android System Optimizer yang ringan, modular, dan stabil. Dioptimasi untuk perangkat low/mid-end Android (mis. Realme 3 — 4GB RAM/64GB storage).

## Fitur
- **RAM Optimizer** — bebaskan memori inactive (panggil GC + ukur delta `availMem`).
- **Cache Cleaner** — bersihkan `cacheDir`, `codeCacheDir`, `externalCacheDir`.
- **Battery Saver** — estimasi penghematan baterai dari kombinasi GC + status `PowerSaveMode`.
- **Process Manager** — stop background process non-system via `killBackgroundProcesses`.
- **History persistence** — 50 entri terakhir tersimpan via SharedPreferences (JSON).
- **Dark Mode override** + **Auto Optimize** preference (persisten).
- **Permission gateway** untuk Usage Access, Battery Optimization Exemption, dan Notifications.

## Arsitektur
```
app/                          # entry point (Activity, Hilt @AndroidEntryPoint)
core/
├── common/                   # konstanta, Result sealed type, BytesFormatter
├── domain/                   # repository interface + use cases (pure Kotlin)
├── data/                     # OptimizationRepositoryImpl, LocalDataSource (SharedPreferences)
└── ui/                       # Compose screens, theme, ViewModel (HiltViewModel)
```

- **MVVM + Clean Architecture** — single `OptimizationViewModel` exposes `StateFlow<OptimizationUiState>` dan `SharedFlow<UiEvent>`. Compose mengonsumsi via `collectAsStateWithLifecycle()`.
- **DI murni Hilt** — `@HiltAndroidApp`, `@AndroidEntryPoint`, `@HiltViewModel`, dan `@Binds` di `AppModule`. Tidak ada repository yang dibuat manual di Composable.
- **Persistence** — `LocalDataSource` adalah satu-satunya yang menyentuh `SharedPreferences`. History diserialisasi via `HistorySerializer` (pure object, JSON).
- **Edge-to-edge** — `enableEdgeToEdge()` + system bars transparan + `values-night/themes.xml` untuk dark variant.
- **Permission re-check on resume** — `LifecycleEventObserver` mendeteksi `ON_RESUME` setelah user kembali dari Settings.

## Tech Stack
- Kotlin 2.0.21 + Jetpack Compose (BOM 2024.10.00)
- Material Design 3 + Material You (dynamic color SDK 31+)
- AndroidX Lifecycle 2.8.4 (`lifecycle-runtime-compose`)
- Hilt 2.51.1 + `hilt-navigation-compose` 1.2.0
- Coroutines + Flow 1.9.0

## Build
```bash
./gradlew assembleDebug             # APK debug
./gradlew check                      # lint + unit test
./gradlew :core:common:test          # bytes formatter test
./gradlew :core:ui:test              # history serializer test
```

### Release signing
Gradle picks up signing via injected properties (`-Pandroid.injected.signing.*`). The CI workflow (`.github/workflows/android-release.yml`) decodes a base64 keystore from secrets and runs `assembleRelease` on `v*` tags.

## CI/CD
- `android-ci.yml` — `./gradlew check` di setiap push/PR ke `main` & `develop`.
- `android-release.yml` — build & publish APK release ke GitHub Releases pada tag `v*`.

## Testing
Unit tests run on the JVM (no Robolectric needed):

- `core:common:BytesFormatterTest` — semua jenis besaran ukuran dan input negatif.
- `core:ui:HistorySerializerTest` — JSON round-trip, malformed input, list kosong.

Tambah test ke modul masing-masing:
```kotlin
testImplementation("junit:junit:4.13.2")
```

## Catatan Refactor (May 2026)
- Hapus QuickActionGrid duplikat di HomeScreen — modul utama sekarang single source of truth.
- Pindahkan state UI ke `OptimizationViewModel` (sebelumnya state hilang setiap rekomposisi/rotasi).
- DI bocor diperbaiki: `OptimizationRepositoryImpl` di-bind via `@Binds` & dikonsumsi melalui use case di VM.
- Dark Mode toggle terhubung ke `SystemOptimizerTheme` via `state.isDarkMode || isSystemInDarkTheme()`.
- Status bar dibuat transparan (edge-to-edge proper, `windowLightStatusBar` mengikut light/dark).
- Icon Process Manager diganti ke `Icons.Default.Apps` (lebih representatif daripada `Close`).
