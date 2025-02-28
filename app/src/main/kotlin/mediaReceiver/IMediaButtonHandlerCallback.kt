package mediaReceiver

import android.view.KeyEvent

interface IMediaButtonHandlerCallback {
    fun onMediaButtonEvent(keyEvent: KeyEvent)
}
