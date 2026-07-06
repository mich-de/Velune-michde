# Velune Android Auto Optimization Plan (7-inch Touch Screens)

This implementation plan outlines the optimizations and architectural alignment needed to make **Velune** fully optimized for Android Auto, with a specific focus on 7-inch vehicle touch screens.

## 1. Context & Architectural Overview

Velune is a Jetpack Compose & Media3-based YouTube Music client. It already utilizes:
- `MusicService` extending `MediaLibraryService` (using a `MediaLibrarySession`).
- `MediaLibrarySessionCallback` implementing `MediaLibrarySession.Callback`.
- Standard Android Auto intent declaration in the manifest via `@xml/automotive_app_desc`.

### Saved System Environment Paths
*   **Android SDK Path:** `C:\Users\mdeangelis\AppData\Local\Android\Sdk`
*   **ADB Executable:** [adb.exe](file:///C:/Users/mdeangelis/AppData/Local/Android/Sdk/platform-tools/adb.exe)
*   **Android Studio Executable:** [studio64.exe](file:///C:/Program%20Files/Android/Android%20Studio/bin/studio64.exe)

To deliver a premium experience on compact **7-inch touch screens** (typically 800x480 or similar resolutions with limited vertical real estate), we must optimize the navigation structure and user interface properties to follow Google's **Driver Distraction Guidelines** and **Media App Best Practices**.

## 2. Goals & Optimizations

### Goal A: Flatten Directory Hierarchy (Fewer Taps)
*   **Problem:** Currently, to access downloaded or liked songs on Android Auto, the user must navigate through `Playlists` and then select `Liked Songs` or `Downloaded Songs`. Under driving safety constraints, this requires too many taps and causes distractions.
*   **Solution:** Expose `Liked Songs` and `Downloaded Songs` directly in `MusicService.ROOT`. This cuts the tap path to just 2 steps (Root -> Songs list -> Play).
*   **Implementation:** Inject these browsable playlists directly into the `onGetChildren` response for `MusicService.ROOT` in `MediaLibrarySessionCallback.kt`.

### Goal B: Optimize Browsable Content Layouts (Content Styles)
*   **Problem:** Standard root categories (Songs, Artists, Albums, Playlists) currently use `CONTENT_STYLE_GRID_ITEM` by default in Velune. For simple, abstract vector icons, this creates unnecessarily large grid blocks on a 7-inch screen.
*   **Solution:** Use `CONTENT_STYLE_LIST_ITEM` for abstract category folders (Songs, Artists, Albums, Playlists) to make them compact and list-friendly, and retain `CONTENT_STYLE_GRID_ITEM` for albums/playlists that have rich graphic album art.
*   **Implementation:** Extend the `browsableMediaItem` helper to accept style hints (`browsableHint` and `playableHint`), then style root-level items as lists and content-heavy folders (like Albums and Playlists) as grids.

### Goal C: Keep Actions Driver-Safe & Ordered
*   **Problem:** Android Auto renders custom action layouts. On small screens, only 1-2 custom action buttons fit on the playback bar; the rest are placed in an overflow menu.
*   **Solution:** Velune already defines a list containing: `Like`, `Repeat`, `Shuffle`, and `Start Radio`. We will review and verify this order to ensure `Like` (highly interactive, driver favorite) remains the first priority.

---

## 3. Implementation Steps

### Phase 1: Update `MediaLibrarySessionCallback.kt`
1. Modify `browsableMediaItem` helper signature to accept custom `browsableHint` and `playableHint`.
2. Update the root folder representation (`MusicService.ROOT` inside `onGetChildren`) to:
   - Include "Liked Songs" and "Downloaded Songs" directly.
   - Set `CONTENT_STYLE_LIST_ITEM` for all root-level browsable folders to preserve horizontal/vertical space.
3. Keep grid-style layouts for actual subfolders (e.g. albums, playlists) where artwork is available.

### Phase 2: Verification and Documentation
1. Create `TASKS.MD` to trace tasks.
2. Update `GEMINI.MD` memory log.
3. Update `README.MD` to document these Android Auto features.

---

## 4. Phase 3: Android Auto Custom UI (Template-based)

### Goal D: Replace Default AA Media UI with Custom Templates
- **Problem:** Android Auto's default media UI is generic and doesn't reflect Velune's branding, content hierarchy, or 7-inch horizontal optimization.
- **Solution:** Implement `CarAppService` + `Session` using the Android for Cars App Library (`androidx.car.app:app`) to render custom `ListTemplate`, `SearchTemplate`, and `PlaybackTemplate` screens.
- **Status:** ✅ **V1 implemented** (browse root + search, plays via `onSetMediaItems`)

### Architecture
- `VeluneCarAppService` — entry point, declared in manifest, creates `VeluneSession`
- `VeluneSession` — connects to `MusicService` via `MediaController`, manages playback
- `BrowseRootScreen` — `ListTemplate` with categories: Liked Songs, Songs, Artists, Albums, Playlists
- `SearchScreen` — `SearchTemplate` with `onSearchSubmitted` callback, plays results

### Files Created
| File | Description |
|------|-------------|
| `app/src/main/kotlin/com/nikhil/yt/car/VeluneCarAppService.kt` | CarAppService entry point |
| `app/src/main/kotlin/com/nikhil/yt/car/VeluneSession.kt` | Session + browse/search screens |

### V2 Features (Planned)
- Sub-category browsing (artist → albums → songs)
- `PlaybackTemplate` with custom controls (like, repeat, shuffle)
- Album art via Coil in car list items
- Database-backed lists instead of `onSetMediaItems` playback
