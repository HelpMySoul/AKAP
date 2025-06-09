package player.managers

import android.view.View
import android.widget.FrameLayout

object FrameLayoutManager {
    private val layouts: MutableList<FrameLayout> = mutableListOf()

    private fun hideLayouts() {
        layouts.forEach {
            it.visibility = View.GONE
        }
    }

    fun openLayout(layout: FrameLayout) {
        if (!layouts.contains(layout)) {
            return
        }

        hideLayouts()

        layout.visibility = View.VISIBLE
    }

    fun addLayouts(vararg layouts: FrameLayout) {
        layouts.forEach { layout ->
            this.layouts.add(layout)
        }
    }
}