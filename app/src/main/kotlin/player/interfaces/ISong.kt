package player.interfaces

import java.util.Date


interface ISong {
    var id:            Long
    var duration:      Long
    var introDuration: Long
    var outroDuration: Long
    val dateAdded:     Long

    var title:         String
    var artist:        String
    val filePath:      String

    var localVolume:   Int
}