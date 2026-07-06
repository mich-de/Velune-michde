

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

### 🎧 YouTube Music client with Android Auto Custom UI

🚫 No Ads • 💰 No Subscription • ⚡ Full Control • 🚗 AA Template-based

<div align="center">

![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![Android Auto](https://img.shields.io/badge/Android%20Auto-3DDC84?style=for-the-badge&logo=androidauto&logoColor=white)
![License](https://img.shields.io/github/license/mich-de/Velune-michde?style=for-the-badge&labelColor=18181B&color=EF4444)

</div>

---

## 🚗 Android Auto Custom UI

This fork adds **template-based** Android Auto support via `androidx.car.app:1.7.0`, optimized for 7-inch horizontal screens.

### What's new
- **`CarAppService` + `VeluneSession`** — replaces Android Auto's default generic UI with custom templates
- **`ListTemplate`** — category browsing: Songs, Artists, Albums, Playlists, Liked Songs
- **`SearchTemplate`** — voice search with Google Assistant integration
- **Optimized navigation** — 2-tap playback from root categories

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

### Planned (V2)
- Sub-category browsing (artist → albums → songs)
- `PlaybackTemplate` with custom controls (like, repeat, shuffle)
- Album art via Coil

---

## 🎯 Highlights

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

This is a fork of [Velune](https://github.com/nikhilvishwakarma00/Velune) by Nikhil.  
All original features are preserved. Customizations live on the `feat/android-auto-ui` branch.

## ⚖️ Legal

Velune is an independent client and is not affiliated with YouTube or Google.  
Licensed under **GPL-3.0**.
