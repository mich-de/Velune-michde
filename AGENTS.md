# Velune — Fork AA Custom UI

## Git
- `origin` = fork (`git@github.com:mich-de/Velune-michde.git`)
- `upstream` = original (`git@github.com:nikhilvishwakarma00/Velune.git`)
- Lavorare sempre su feature branch, mai su `main`
- Sync upstream: `git fetch upstream && git rebase upstream/main`

## Build (SSL + JDK)
Env ha SSL cert issue → `gradlew` fallisce. Usare Gradle dal cache:

```powershell
$env:JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"
& "$env:USERPROFILE\.gradle\wrapper\dists\gradle-9.5.1-all\f1duamibpuutrzlxrpekwo5vz\gradle-9.5.1\bin\gradle.bat" assembleArm64Debug
```

## Struttura
- App module `:app` + 7 librerie (`:innertube`, `:kugou`, `:lrclib`, `:lastfm`, `:simpmusic`, `:betterlyrics`, `:kizzy`, `:canvas`)
- `MusicService` (MediaLibraryService) + `MediaLibrarySessionCallback` — backbone AA playback
- `AndroidManifest` dichiara `com.google.android.gms.car.application` + `CarAppService`
- ADB: `C:\Users\mdeangelis\AppData\Local\Android\Sdk\platform-tools\adb.exe`
- Tests JUnit4: `gradlew :app:testArm64DebugUnitTest`

## Android Auto (feat/android-auto-ui)
- `car/VeluneCarAppService.kt` — extends `CarAppService`, crea `VeluneSession`
- `car/VeluneSession.kt` — templates: `ListTemplate` (root categorie) + `SearchTemplate`
- Playback via `MediaController.setMediaItems()` → risolto da `onSetMediaItems` nel callback
- `MediaController` **è** `Player` (implementa l'interfaccia)
- No `Player.UNSET_TIME` — usare `C.TIME_UNSET`
- `SearchTemplate.Builder` richiede `SearchCallback` esplicito (lambda non supportata)

## Da sapere
- `minSdk=26`, `targetSdk=36`, Kotlin 2.3.10, Media3 1.9.2
- Hilt + KSP per DI
- Equalizzatore nativo Android (`audiofx`) già implementato
- Crossfade via `CrossfadeAudioProcessor` + overlap player
- 6 provider lyrics (ttml, lrc, kugou, youtube, simpmusic)
- Last.fm + ListenBrainz scrobbling
- Temi Material You + custom HCT color
- `media3-session`: `MediaLibraryController` **NON esiste** (rimosso)
