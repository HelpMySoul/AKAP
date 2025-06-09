package topMenu

class TopMenuButton(val name:     String,
                    val function: () -> Unit) {
    fun action() {
        function()
    }
}
