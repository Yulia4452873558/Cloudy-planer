package com.example.planer.data

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaController
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionToken
import com.example.planer.MainActivity
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture

/** Трек из папки app/src/main/assets/music. */
data class Track(val file: String, val title: String, val durationMs: Long)

/**
 * Музыка. Список треков берётся из assets/music — чтобы добавить трек,
 * достаточно положить туда mp3. Название читается из тегов файла.
 * Играет сервис [PlaybackService], поэтому музыка не останавливается,
 * когда экран выключен или приложение свёрнуто.
 */
object Music {
    private const val DIR = "music"

    var tracks by mutableStateOf<List<Track>>(emptyList())
        private set
    var playing by mutableStateOf(false)
        private set
    var index by mutableIntStateOf(-1)
        private set
    var position by mutableLongStateOf(0L)
        private set
    var duration by mutableLongStateOf(0L)
        private set

    private var controller: MediaController? = null
    private var future: ListenableFuture<MediaController>? = null

    private val listener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            sync()
        }
    }

    fun uriFor(file: String): String = "asset:///$DIR/$file"

    /** Читает список треков (один раз). */
    fun load(ctx: Context) {
        if (tracks.isNotEmpty()) return
        val names = try {
            ctx.assets.list(DIR).orEmpty().toList()
        } catch (e: Exception) {
            emptyList()
        }
        val files = names.filter { n -> listOf(".mp3", ".m4a", ".ogg", ".wav", ".aac").any { n.endsWith(it, ignoreCase = true) } }
        // titles.txt: строки «файл.mp3=Название». Порядок строк = порядок в списке.
        val titles = readTitles(ctx)
        val ordered = titles.keys.filter { it in files } + files.filter { it !in titles.keys }.sorted()
        tracks = ordered.map { readTrack(ctx, it, titles[it]) }
    }

    private fun readTitles(ctx: Context): Map<String, String> {
        val map = LinkedHashMap<String, String>()
        try {
            ctx.assets.open("$DIR/titles.txt").bufferedReader(Charsets.UTF_8).useLines { lines ->
                lines.forEach { raw ->
                    val line = raw.trim().removePrefix("\uFEFF")
                    val i = line.indexOf('=')
                    if (line.isNotEmpty() && !line.startsWith("#") && i > 0) {
                        map[line.substring(0, i).trim()] = line.substring(i + 1).trim()
                    }
                }
            }
        } catch (e: Exception) {
            // Файла нет — названия возьмём из тегов mp3.
        }
        return map
    }

    private fun readTrack(ctx: Context, name: String, custom: String?): Track {
        var title: String? = custom
        var dur = 0L
        try {
            ctx.assets.openFd("$DIR/$name").use { fd ->
                val r = MediaMetadataRetriever()
                try {
                    r.setDataSource(fd.fileDescriptor, fd.startOffset, fd.length)
                    if (title == null) title = r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                    dur = r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
                } finally {
                    r.release()
                }
            }
        } catch (e: Exception) {
            // Файл сжат или без тегов — возьмём имя файла.
        }
        val fallback = name.substringBeforeLast('.').replace('_', ' ').replace('-', ' ').trim()
            .replaceFirstChar { it.uppercase() }
        return Track(name, title?.takeIf { it.isNotBlank() } ?: fallback, dur)
    }

    /** Подключаемся к сервису музыки (вызывается в onStart активности). */
    fun connect(ctx: Context) {
        load(ctx)
        if (future != null) return
        val token = SessionToken(ctx, ComponentName(ctx, PlaybackService::class.java))
        val f = MediaController.Builder(ctx, token).buildAsync()
        future = f
        f.addListener({
            try {
                val c = f.get()
                controller = c
                c.addListener(listener)
                sync()
            } catch (e: Exception) {
                future = null
            }
        }, ContextCompat.getMainExecutor(ctx))
    }

    /** Отключаемся от сервиса (onStop). Музыка при этом продолжает играть. */
    fun disconnect() {
        controller?.removeListener(listener)
        future?.let { MediaController.releaseFuture(it) }
        future = null
        controller = null
    }

    fun sync() {
        val c = controller ?: return
        playing = c.isPlaying
        val id = c.currentMediaItem?.mediaId
        index = if (id == null) -1 else tracks.indexOfFirst { it.file == id }
        position = c.currentPosition.coerceAtLeast(0L)
        val d = c.duration
        duration = if (d > 0) d else tracks.getOrNull(index)?.durationMs ?: 0L
    }

    private fun item(t: Track): MediaItem =
        MediaItem.Builder()
            .setMediaId(t.file)
            .setUri(uriFor(t.file))
            .setMediaMetadata(MediaMetadata.Builder().setTitle(t.title).setArtist("Cloudy").build())
            .build()

    /** Включить трек номер [i] (весь список играет по кругу). */
    fun play(i: Int) {
        val c = controller ?: return
        if (i !in tracks.indices) return
        val sameList = c.mediaItemCount == tracks.size &&
            tracks.indices.all { c.getMediaItemAt(it).mediaId == tracks[it].file }
        if (sameList) {
            c.seekTo(i, 0L)
        } else {
            c.setMediaItems(tracks.map { item(it) }, i, 0L)
            c.repeatMode = Player.REPEAT_MODE_ALL
            c.prepare()
        }
        c.play()
    }

    fun toggle() {
        val c = controller ?: return
        if (c.mediaItemCount == 0) {
            play(0)
            return
        }
        if (c.isPlaying) c.pause() else c.play()
    }

    fun next() {
        val c = controller ?: return
        if (c.mediaItemCount == 0) play(0) else c.seekToNextMediaItem()
    }

    fun prev() {
        val c = controller ?: return
        if (c.mediaItemCount == 0) play(0) else c.seekToPreviousMediaItem()
    }

    fun seekTo(fraction: Float) {
        val c = controller ?: return
        if (duration > 0) c.seekTo((duration * fraction.coerceIn(0f, 1f)).toLong())
    }
}

/** Фоновый сервис: играет музыку и показывает управление в шторке и на экране блокировки. */
class PlaybackService : MediaSessionService() {
    private var session: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        val player = ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder().setUsage(C.USAGE_MEDIA).setContentType(C.AUDIO_CONTENT_TYPE_MUSIC).build(),
                true
            )
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .build()
        player.repeatMode = Player.REPEAT_MODE_ALL
        val open = PendingIntent.getActivity(
            this, 2, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        session = MediaSession.Builder(this, player)
            .setSessionActivity(open)
            .setCallback(object : MediaSession.Callback {
                // Адрес файла восстанавливаем по id — так надёжнее.
                override fun onAddMediaItems(
                    mediaSession: MediaSession,
                    controller: MediaSession.ControllerInfo,
                    mediaItems: MutableList<MediaItem>
                ): ListenableFuture<MutableList<MediaItem>> =
                    Futures.immediateFuture(
                        mediaItems.map { it.buildUpon().setUri(Music.uriFor(it.mediaId)).build() }.toMutableList()
                    )
            })
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = session

    override fun onTaskRemoved(rootIntent: Intent?) {
        val p = session?.player
        if (p == null || !p.playWhenReady || p.mediaItemCount == 0) stopSelf()
    }

    override fun onDestroy() {
        session?.run {
            player.release()
            release()
        }
        session = null
        super.onDestroy()
    }
}
