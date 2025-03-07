package mediaReceiver

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.media.session.MediaSession
import android.os.Build
import android.util.Log
import android.view.KeyEvent
import broadcast.BroadcastManagerController
import playlistMenu.managers.SongManager

class MediaButtonHandler(context: Context): IMediaButtonHandlerCallback {

    private val appContext: Context = context.applicationContext
    private var startApp:   Boolean = true

    private lateinit var mediaButtonReceiver: MediaButtonReceiver
    private lateinit var mediaSession:        MediaSession

    fun initialize() {
        registerReceiver()
        setupSession()
    }

    fun release() {
        unregisterReceiver()
        releaseSession()
        Log.e("NotificationServiceError","Destroyed MBH")
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private fun registerReceiver() {
        mediaButtonReceiver = MediaButtonReceiver()
        val filter = IntentFilter(Intent.ACTION_MEDIA_BUTTON)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            appContext.registerReceiver(mediaButtonReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            appContext.registerReceiver(mediaButtonReceiver, filter)
        }
    }

    private fun unregisterReceiver() {
        appContext.unregisterReceiver(mediaButtonReceiver)
    }

    private fun setupSession() {
        mediaSession = MediaSession(appContext, "MediaButtonHandler")

        mediaSession.setCallback(object : MediaSession.Callback() {
            override fun onMediaButtonEvent(mediaButtonIntent: Intent): Boolean {
                val keyEvent = mediaButtonIntent.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT)
                if (keyEvent?.action == KeyEvent.ACTION_DOWN) {
                    handleEvent(keyEvent)
                }
                return super.onMediaButtonEvent(mediaButtonIntent)
            }
        })

        mediaSession.isActive = true

        val audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.registerMediaButtonEventReceiver(
            ComponentName(appContext, MediaButtonReceiver::class.java)
        )

        audioManager.registerAudioDeviceCallback(object : AudioDeviceCallback() {
            override fun onAudioDevicesAdded(addedDevices: Array<AudioDeviceInfo>) {
                handleAudioDeviceChange()
            }

            override fun onAudioDevicesRemoved(removedDevices: Array<AudioDeviceInfo>) {
                handleAudioDeviceChange()
            }
        }, null)
    }

    private fun handleAudioDeviceChange() {
        if (!startApp) {
            startApp = true
        } else {
            startApp = false
        }

        Log.e("NotificationServiceError", "DeviceChanged")
    }

    private fun releaseSession() {
        if (::mediaSession.isInitialized) {
            val audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audioManager.unregisterMediaButtonEventReceiver(
                ComponentName(appContext, MediaButtonReceiver::class.java)
            )
            audioManager.unregisterAudioDeviceCallback(null);

            mediaSession.isActive = false
            mediaSession.release()
        }
    }

    override fun onMediaButtonEvent(keyEvent: KeyEvent) {
        handleEvent(keyEvent)
    }

    private fun handleEvent(keyEvent: KeyEvent) {
        Log.d("MediaEvent", "Event: ${keyEvent.keyCode}")
        when (keyEvent.keyCode) {
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                BroadcastManagerController(appContext).sendBroadcast("PAUSE_OR_PLAY_SONG")
            }
            KeyEvent.KEYCODE_MEDIA_PLAY -> {
                BroadcastManagerController(appContext).sendBroadcast("PAUSE_OR_PLAY_SONG")
            }
            KeyEvent.KEYCODE_MEDIA_PAUSE -> {
                BroadcastManagerController(appContext).sendBroadcast("PAUSE_OR_PLAY_SONG")
            }
            KeyEvent.KEYCODE_MEDIA_NEXT -> {
                BroadcastManagerController(appContext).sendBroadcast("NEXT_SONG")
            }
            KeyEvent.KEYCODE_MEDIA_PREVIOUS -> {
                BroadcastManagerController(appContext).sendBroadcast("PREVIOUS_SONG")
            }
        }
    }
}