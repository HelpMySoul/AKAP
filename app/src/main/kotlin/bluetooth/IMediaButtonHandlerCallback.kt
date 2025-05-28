package bluetooth

import android.view.KeyEvent

interface IMediaButtonHandlerCallback {
    fun onMediaButtonEvent(keyEvent: KeyEvent)
}
