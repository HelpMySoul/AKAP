package playlistMenu.interfaces

import java.io.Serializable


interface ISong {
    val id:            Long
    var duration:      Long
    var introDuration: Long
    var outroDuration: Long

    val title:         String
    val artist:        String
    val filePath:      String

    var localVolume:   Int
}