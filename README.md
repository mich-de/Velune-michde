

<div align="center">

 <img src="https://raw.githubusercontent.com/nikhilvishwakarma00/Velune/main/fastlane/metadata/android/en-US/images/icon.png" width="110" />

</div>

# 🌌 Velune — mich-de fork
<div align="center">

<pre>
██╗   ██╗███████╗██╗     ██╗   ██╗███╗   ██╗███████╗
██║   ██║██╔════╝██║     ██║   ██║████╗  ██║██╔════╝
██║   ██║█████╗  ██║     ██║   ██║██╔██╗ ██║█████╗
╚██╗ ██╔╝██╔══╝  ██║     ██║   ██║██║╚██╗██║██╔══╝
 ╚████╔╝ ███████╗███████╗╚██████╔╝██║ ╚████║███████╗
  ╚═══╝  ╚══════╝╚══════╝ ╚═════╝ ╚═╝  ╚═══╝╚══════╝
</pre>

</div>

### 🎧 YouTube Music — con Android Auto Custom UI

🚫 No Ads • 💰 No Subscription • ⚡ Full Control • 🚗 AA Template-based

<div align="center">

![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![Android Auto](https://img.shields.io/badge/Android%20Auto-3DDC84?style=for-the-badge&logo=androidauto&logoColor=white)
![License](https://img.shields.io/github/license/mich-de/Velune-michde?style=for-the-badge&labelColor=18181B&color=EF4444)

</div>

---

## 🚗 Android Auto Custom UI

Questo fork aggiunge il supporto **template-based** per Android Auto con `androidx.car.app:1.7.0`, ottimizzato per schermi 7" orizzontali.

### Novità
- **`CarAppService` + `VeluneSession`** — sostituisce la UI generica di Android Auto con template custom
- **`ListTemplate`** — navigazione categorie: Brani, Artisti, Album, Playlist, Preferiti
- **`SearchTemplate`** — ricerca vocale integrata con Google Assistant
- **Navigazione ottimizzata** — percorsi ridotti a 2 tap per iniziare la riproduzione

### Architecture
```
CarAppService → VeluneSession → BrowseRootScreen (ListTemplate)
                               → SearchScreen (SearchTemplate)
                               ↓
                          MediaController (Player)
                               ↓
                    MusicService (MediaLibraryService)
                               ↓
               MediaLibrarySessionCallback.onSetMediaItems()
```

### V2 (in arrivo)
- Sub-category browsing (artista → album → brani)
- `PlaybackTemplate` con controlli custom (like, repeat, shuffle)
- Album art nelle liste tramite Coil

---

## 🎯 Highlight Feature

### 🎤 Real-Time Synced Lyrics

- Word-by-word sync
- Smooth animations
- Translation support
- Fully immersive playback

---

## ✨ Features

### 🎵 Core Experience
- Ad-Free Playback
- Full Library Sync
- Offline Caching
- Background Playback

### 🔊 Audio Engine
- Gapless Playback
- Crossfade Engine
- Silence Skipping
- Loudness Normalization (EBU R128)
- Tempo & Pitch Control
- System EQ Integration

### 🎨 UI & Discovery
- Material You (Dynamic Colors)
- Synced Lyrics + Translation
- Personalized Home Feed
- Year in Review Stats

---

## 🧠 Architecture

- MVVM + Clean Architecture
- Unidirectional Data Flow (UDF)
- Modular & scalable codebase

## 🛠 Tech Stack

| Layer | Stack |
|------|------|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Audio | Media3 / ExoPlayer |
| DI | Hilt |
| Database | Room |
| Networking | Ktor |
| Async | Coroutines + Flow |
| Car | androidx.car.app:1.7.0 |

## 📂 Project Structure

```bash
velune/
├── app/
│   ├── car/               ← CarAppService + Session + Screens
│   ├── playback/           ← MusicService + MediaLibrarySessionCallback
│   └── ui/                 ← Compose UI
├── innertube/              ← YouTube API client
├── lrclib/                 ← LRC lyrics
├── betterlyrics/           ← TTML lyrics
├── kugou/                  ← KuGou lyrics
├── lastfm/                 ← Last.fm scrobbling
├── simpmusic/              ← SimpMusic lyrics
├── kizzy/                  ← Discord RPC
└── canvas/                 ← Animated artwork
```

---

## 🔗 Original Project

Questo è un fork di [Velune](https://github.com/nikhilvishwakarma00/Velune) by Nikhil.  
Tutte le feature originali sono preservate. Le modifiche sono nel branch `feat/android-auto-ui`.

## ⚖️ Legal

Velune is an independent client and is not affiliated with YouTube or Google.  
Licensed under **GPL-3.0**.
