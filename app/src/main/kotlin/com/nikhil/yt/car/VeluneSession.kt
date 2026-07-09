package com.nikhil.yt.car

import android.content.ComponentName
import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.Session
import androidx.car.app.model.Action
import androidx.car.app.model.ActionStrip
import androidx.car.app.model.CarIcon
import androidx.car.app.model.ItemList
import androidx.car.app.model.ListTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.SearchTemplate
import androidx.car.app.model.Template
import androidx.core.graphics.drawable.IconCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaBrowser
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.nikhil.yt.R
import com.nikhil.yt.playback.MusicService

private val handler = Handler(Looper.getMainLooper())

class VeluneSession : Session() {
    companion object {
        @Volatile
        var instance: VeluneSession? = null
    }

    var mediaBrowser: MediaBrowser? = null
        private set

    private var browserFuture: ListenableFuture<MediaBrowser>? = null

    fun logError(message: String, e: Throwable? = null) {
        try {
            println("VeluneCarApp: $message")
            val logFile = java.io.File(carContext.cacheDir, "car_log.txt")
            val time = java.time.LocalDateTime.now().toString()
            val errText = e?.let { "\n${android.util.Log.getStackTraceString(it)}" } ?: ""
            logFile.appendText("[$time] $message$errText\n")
        } catch (_: Exception) {}
    }

    override fun onCreateScreen(intent: Intent): Screen {
        instance = this
        connectToMediaService()
        return BrowseRootScreen(carContext)
    }

    private fun connectToMediaService() {
        logError("Connecting to MediaService...")
        try {
            val appCtx = carContext.applicationContext
            val token = SessionToken(appCtx, ComponentName(appCtx, MusicService::class.java))
            val future = MediaBrowser.Builder(appCtx, token).buildAsync()
            browserFuture = future
            future.addListener(
                {
                    try {
                        mediaBrowser = future.get()
                        logError("Connected successfully to MediaBrowser!")
                    } catch (e: Exception) {
                        logError("Failed to get MediaBrowser inside future listener", e)
                    }
                },
                MoreExecutors.directExecutor()
            )
        } catch (e: Exception) {
            logError("Failed to initiate MediaBrowser connection", e)
        }
    }

    fun playCategory(mediaId: String) {
        val browser = mediaBrowser ?: return
        val item = MediaItem.Builder()
            .setMediaId(mediaId)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setIsBrowsable(true)
                    .build()
            )
            .build()
        browser.setMediaItems(listOf(item))
        browser.prepare()
        browser.play()
    }

    override fun onCarConfigurationChanged(newConfig: android.content.res.Configuration) {
        instance = this
    }
}

private abstract class BaseScreen(carContext: CarContext) : Screen(carContext) {
    protected val session: VeluneSession?
        get() = VeluneSession.instance
    protected val carCtx: CarContext = carContext
}

private class BrowseRootScreen(carContext: CarContext) : BaseScreen(carContext) {
    override fun onGetTemplate(): Template {
        val searchAction = Action.Builder()
            .setIcon(CarIcon.Builder(
                IconCompat.createWithResource(carCtx, android.R.drawable.ic_menu_search)
            ).build())
            .setOnClickListener { screenManager.push(SearchScreen(carCtx)) }
            .build()

        val itemList = ItemList.Builder().apply {
            addItem(categoryRow(
                "Preferiti",
                R.drawable.favorite,
                "playlists/0"
            ))
            addItem(categoryRow(
                "Scaricate",
                R.drawable.download,
                "playlists/2"
            ))
            addItem(categoryRow(
                "Storia",
                R.drawable.history,
                "recent"
            ))
            addItem(categoryRow(
                "Coda",
                R.drawable.queue_music,
                "queue"
            ))
            addItem(categoryRow(
                "Testo",
                R.drawable.translate,
                "lyrics"
            ))
            addItem(categoryRow(
                "Libreria",
                R.drawable.library_music,
                "library"
            ))
        }.build()

        return ListTemplate.Builder()
            .setTitle(carCtx.getString(R.string.app_name))
            .setHeaderAction(Action.APP_ICON)
            .setActionStrip(ActionStrip.Builder().addAction(searchAction).build())
            .setSingleList(itemList)
            .build()
    }

    private fun categoryRow(title: String, iconRes: Int, mediaId: String): Row =
        Row.Builder()
            .setTitle(title)
            .setImage(
                CarIcon.Builder(IconCompat.createWithResource(carCtx, iconRes)).build(),
                Row.IMAGE_TYPE_SMALL
            )
            .setOnClickListener {
                if (mediaId == "lyrics") {
                    screenManager.push(NowPlayingScreen(carCtx))
                } else {
                    screenManager.push(BrowseScreen(carCtx, mediaId, title))
                }
            }
            .build()
}

private class BrowseScreen(
    carContext: CarContext,
    val parentId: String,
    val title: String
) : BaseScreen(carContext) {
    private var items: List<MediaItem> = emptyList()
    private var isLoading = true

    init {
        loadChildren()
    }

    private fun loadChildren() {
        val browser = session?.mediaBrowser
        if (browser == null) {
            handler.postDelayed({ loadChildren() }, 500)
            return
        }
        val future = browser.getChildren(parentId, 0, 100, null)
        future.addListener({
            try {
                val result = future.get()
                items = result.value ?: emptyList()
            } catch (e: Exception) {
                session?.logError("loadChildren failed for parentId = $parentId", e)
                items = emptyList()
            }
            isLoading = false
            invalidate()
        }, MoreExecutors.directExecutor())
    }

    override fun onGetTemplate(): Template {
        if (isLoading) {
            return ListTemplate.Builder()
                .setTitle(title)
                .setHeaderAction(Action.BACK)
                .setLoading(true)
                .build()
        }

        val itemListBuilder = ItemList.Builder()
        if (items.isEmpty()) {
            itemListBuilder.setNoItemsMessage("Empty")
        } else {
            for (item in items) {
                val isBrowsable = item.mediaMetadata.isBrowsable == true
                val rowBuilder = Row.Builder()
                    .setTitle(item.mediaMetadata.title?.toString() ?: "Unknown Song")
                    .addText(item.mediaMetadata.artist?.toString() ?: item.mediaMetadata.albumTitle?.toString() ?: "")

                if (isBrowsable) {
                    val iconRes = when {
                        item.mediaId.startsWith("artists") -> R.drawable.artist
                        item.mediaId.startsWith("albums") -> R.drawable.album
                        else -> R.drawable.queue_music
                    }
                    rowBuilder.setImage(
                        CarIcon.Builder(IconCompat.createWithResource(carCtx, iconRes)).build(),
                        Row.IMAGE_TYPE_SMALL
                    )
                    rowBuilder.setOnClickListener {
                        screenManager.push(BrowseScreen(carCtx, item.mediaId, item.mediaMetadata.title?.toString() ?: ""))
                    }
                } else {
                    rowBuilder.setImage(
                        CarIcon.Builder(IconCompat.createWithResource(carCtx, R.drawable.music_note)).build(),
                        Row.IMAGE_TYPE_SMALL
                    )
                    rowBuilder.setOnClickListener {
                        playSong(item)
                    }
                }
                itemListBuilder.addItem(rowBuilder.build())
            }
        }

        val builder = ListTemplate.Builder()
            .setTitle(title)
            .setHeaderAction(Action.BACK)
            .setSingleList(itemListBuilder.build())

        if (parentId == "lyrics") {
            builder.setActionStrip(
                ActionStrip.Builder()
                    .addAction(
                        Action.Builder()
                            .setTitle("Traduci")
                            .setIcon(CarIcon.Builder(IconCompat.createWithResource(carCtx, R.drawable.translate)).build())
                            .setOnClickListener {
                                val browser = session?.mediaBrowser
                                if (browser != null) {
                                    browser.sendCustomCommand(
                                        com.nikhil.yt.constants.MediaSessionConstants.CommandTranslateLyrics,
                                        android.os.Bundle.EMPTY
                                    )
                                    isLoading = true
                                    invalidate()
                                    handler.postDelayed({ loadChildren() }, 3000)
                                }
                            }
                            .build()
                    )
                    .build()
            )
        }

        return builder.build()
    }

    private fun playSong(item: MediaItem) {
        val browser = session?.mediaBrowser ?: return
        browser.setMediaItems(listOf(item))
        browser.prepare()
        browser.play()
    }
}

private class SearchScreen(carContext: CarContext) : BaseScreen(carContext) {
    override fun onGetTemplate(): Template {
        return SearchTemplate.Builder(
            object : SearchTemplate.SearchCallback {
                override fun onSearchSubmitted(searchTerm: String) {
                    if (searchTerm.isNotBlank()) {
                        screenManager.push(SearchResultScreen(carCtx, searchTerm))
                    }
                }
            }
        )
            .setHeaderAction(Action.BACK)
            .build()
    }
}

private class SearchResultScreen(
    carContext: CarContext,
    val query: String
) : BaseScreen(carContext) {
    private var items: List<MediaItem> = emptyList()
    private var isLoading = true

    init {
        loadSearchResults()
    }

    private fun loadSearchResults() {
        val browser = session?.mediaBrowser
        if (browser == null) {
            handler.postDelayed({ loadSearchResults() }, 500)
            return
        }
        
        val searchFuture = browser.search(query, null)
        searchFuture.addListener({
            val resultFuture = browser.getSearchResult(query, 0, 100, null)
            resultFuture.addListener({
                try {
                val result = resultFuture.get()
                items = result.value ?: emptyList()
            } catch (e: Exception) {
                session?.logError("loadSearchResults failed for query = $query", e)
                items = emptyList()
            }
            isLoading = false
            invalidate()
            }, MoreExecutors.directExecutor())
        }, MoreExecutors.directExecutor())
    }

    override fun onGetTemplate(): Template {
        val resultsTitle = "Search Results"
        if (isLoading) {
            return ListTemplate.Builder()
                .setTitle(resultsTitle)
                .setHeaderAction(Action.BACK)
                .setLoading(true)
                .build()
        }

        val itemListBuilder = ItemList.Builder()
        if (items.isEmpty()) {
            itemListBuilder.setNoItemsMessage("No results found")
        } else {
            for (item in items) {
                val isBrowsable = item.mediaMetadata.isBrowsable == true
                val rowBuilder = Row.Builder()
                    .setTitle(item.mediaMetadata.title?.toString() ?: "Unknown Song")
                    .addText(item.mediaMetadata.artist?.toString() ?: item.mediaMetadata.albumTitle?.toString() ?: "")

                if (isBrowsable) {
                    val iconRes = when {
                        item.mediaId.startsWith("artists") -> R.drawable.artist
                        item.mediaId.startsWith("albums") -> R.drawable.album
                        else -> R.drawable.queue_music
                    }
                    rowBuilder.setImage(
                        CarIcon.Builder(IconCompat.createWithResource(carCtx, iconRes)).build(),
                        Row.IMAGE_TYPE_SMALL
                    )
                    rowBuilder.setOnClickListener {
                        screenManager.push(BrowseScreen(carCtx, item.mediaId, item.mediaMetadata.title?.toString() ?: ""))
                    }
                } else {
                    rowBuilder.setImage(
                        CarIcon.Builder(IconCompat.createWithResource(carCtx, R.drawable.music_note)).build(),
                        Row.IMAGE_TYPE_SMALL
                    )
                    rowBuilder.setOnClickListener {
                        playSong(item)
                    }
                }
                itemListBuilder.addItem(rowBuilder.build())
            }
        }

        return ListTemplate.Builder()
            .setTitle(resultsTitle)
            .setHeaderAction(Action.BACK)
            .setSingleList(itemListBuilder.build())
            .build()
    }

    private fun playSong(item: MediaItem) {
        val browser = session?.mediaBrowser ?: return
        browser.setMediaItems(listOf(item))
        browser.prepare()
        browser.play()
    }
}

private class NowPlayingScreen(carContext: CarContext) : BaseScreen(carContext) {
    private var lyricsText: String = "Caricamento testo..."
    private var songTitle = "Nessun brano"
    private var songArtist = ""
    private var isPlaying = false
    private var currentSongId: String? = null

    private val playerListener = object : Player.Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            updateCurrentSong()
        }

        override fun onIsPlayingChanged(playing: Boolean) {
            isPlaying = playing
            invalidate()
        }
    }

    init {
        lifecycle.addObserver(androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_DESTROY) {
                session?.mediaBrowser?.removeListener(playerListener)
            }
        })
        handler.post {
            val browser = session?.mediaBrowser
            if (browser != null) {
                browser.addListener(playerListener)
                isPlaying = browser.isPlaying
                updateCurrentSong()
            } else {
                checkBrowserConnection()
            }
        }
    }

    private fun checkBrowserConnection() {
        val browser = session?.mediaBrowser
        if (browser != null) {
            browser.addListener(playerListener)
            isPlaying = browser.isPlaying
            updateCurrentSong()
        } else {
            handler.postDelayed({ checkBrowserConnection() }, 500)
        }
    }

    private fun updateCurrentSong() {
        val browser = session?.mediaBrowser ?: return
        val currentItem = browser.currentMediaItem
        if (currentItem == null) {
            songTitle = "Nessun brano in riproduzione"
            songArtist = ""
            lyricsText = ""
            currentSongId = null
            invalidate()
            return
        }

        val title = currentItem.mediaMetadata.title?.toString() ?: "Sconosciuto"
        val artist = currentItem.mediaMetadata.artist?.toString() ?: ""
        val mediaId = currentItem.mediaId
        val songId = mediaId.substringAfterLast("/")

        if (songId == currentSongId) return
        currentSongId = songId
        songTitle = title
        songArtist = artist
        lyricsText = "Caricamento testo..."
        invalidate()

        fetchLyricsFromBrowser()
    }

    private fun fetchLyricsFromBrowser() {
        val browser = session?.mediaBrowser ?: return
        val future = browser.getChildren("lyrics", 0, 200, null)
        future.addListener({
            try {
                val result = future.get()
                val items = result.value ?: emptyList()
                if (items.isEmpty()) {
                    lyricsText = "Testo non trovato"
                } else if (items.size == 1 && (items[0].mediaId == "lyrics/no_track" || items[0].mediaId == "lyrics/not_found")) {
                    lyricsText = items[0].mediaMetadata.title?.toString() ?: "Testo non trovato"
                } else {
                    lyricsText = items.joinToString("\n") { it.mediaMetadata.title?.toString() ?: "" }
                }
            } catch (e: Exception) {
                lyricsText = "Errore durante il caricamento"
            }
            invalidate()
        }, MoreExecutors.directExecutor())
    }

    override fun onGetTemplate(): Template {
        val browser = session?.mediaBrowser
        val playPauseIconRes = if (isPlaying) R.drawable.pause else R.drawable.play
        
        val builder = androidx.car.app.model.LongMessageTemplate.Builder(
            if (lyricsText.isBlank()) "Avvia la musica per vedere il testo" else lyricsText
        )
            .setTitle("$songTitle${if (songArtist.isNotEmpty()) " - $songArtist" else ""}")
            .setHeaderAction(Action.BACK)
            
        builder.setActionStrip(
            ActionStrip.Builder()
                .addAction(
                    Action.Builder()
                        .setTitle("Traduci")
                        .setIcon(CarIcon.Builder(IconCompat.createWithResource(carCtx, R.drawable.translate)).build())
                        .setOnClickListener {
                            if (browser != null) {
                                browser.sendCustomCommand(
                                    com.nikhil.yt.constants.MediaSessionConstants.CommandTranslateLyrics,
                                    android.os.Bundle.EMPTY
                                )
                                lyricsText = "Traduzione in corso..."
                                invalidate()
                                handler.postDelayed({ fetchLyricsFromBrowser() }, 3000)
                            }
                        }
                        .build()
                )
                .build()
        )

        builder.addAction(
            Action.Builder()
                .setIcon(CarIcon.Builder(IconCompat.createWithResource(carCtx, R.drawable.skip_previous)).build())
                .setOnClickListener {
                    browser?.seekToPrevious()
                }
                .build()
        )
        builder.addAction(
            Action.Builder()
                .setIcon(CarIcon.Builder(IconCompat.createWithResource(carCtx, playPauseIconRes)).build())
                .setOnClickListener {
                    if (isPlaying) browser?.pause() else browser?.play()
                }
                .build()
        )
        builder.addAction(
            Action.Builder()
                .setIcon(CarIcon.Builder(IconCompat.createWithResource(carCtx, R.drawable.skip_next)).build())
                .setOnClickListener {
                    browser?.seekToNext()
                }
                .build()
        )

        return builder.build()
    }
}