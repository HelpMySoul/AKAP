package mediaReceiver

import android.view.KeyEvent

interface MediaButtonHandlerCallback {
    fun onMediaButtonEvent(keyEvent: KeyEvent)
}
