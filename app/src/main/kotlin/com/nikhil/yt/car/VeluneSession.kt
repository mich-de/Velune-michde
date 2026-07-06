package com.nikhil.yt.car

import android.content.ComponentName
import android.content.Intent
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
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.nikhil.yt.R
import com.nikhil.yt.playback.MusicService

class VeluneSession : Session() {
    companion object {
        @Volatile
        var instance: VeluneSession? = null
    }

    var mediaController: MediaController? = null
        private set

    private var controllerFuture: ListenableFuture<MediaController>? = null

    override fun onCreateScreen(intent: Intent): Screen {
        instance = this
        connectToMediaService()
        return BrowseRootScreen(carContext)
    }

    private fun connectToMediaService() {
        val token = SessionToken(carContext, ComponentName(carContext, MusicService::class.java))
        val future = MediaController.Builder(carContext, token).buildAsync()
        controllerFuture = future
        future.addListener(
            {
                try {
                    mediaController = Futures.getChecked(future, RuntimeException::class.java)
                } catch (_: Exception) { }
            },
            MoreExecutors.directExecutor()
        )
    }

    fun playCategory(mediaId: String) {
        val ctrl = mediaController ?: return
        val item = MediaItem.Builder()
            .setMediaId(mediaId)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setIsBrowsable(true)
                    .build()
            )
            .build()
        ctrl.setMediaItems(listOf(item))
        ctrl.prepare()
        ctrl.play()
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
                carCtx.getString(R.string.liked_songs),
                android.R.drawable.btn_star,
                "playlists/0"
            ))
            addItem(categoryRow(
                carCtx.getString(R.string.songs),
                android.R.drawable.ic_menu_sort_by_size,
                "songs"
            ))
            addItem(categoryRow(
                carCtx.getString(R.string.artists),
                android.R.drawable.ic_menu_gallery,
                "artists"
            ))
            addItem(categoryRow(
                carCtx.getString(R.string.albums),
                android.R.drawable.ic_menu_gallery,
                "albums"
            ))
            addItem(categoryRow(
                carCtx.getString(R.string.playlists),
                android.R.drawable.ic_menu_manage,
                "playlists"
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
            .setOnClickListener { session?.playCategory(mediaId) }
            .build()
}

private class SearchScreen(carContext: CarContext) : BaseScreen(carContext) {
    override fun onGetTemplate(): Template {
        return SearchTemplate.Builder(
            object : SearchTemplate.SearchCallback {
                override fun onSearchSubmitted(searchTerm: String) {
                    if (searchTerm.isNotBlank()) {
                        session?.playCategory(searchTerm)
                    }
                }
            }
        )
            .setHeaderAction(Action.BACK)
            .build()
    }
}
