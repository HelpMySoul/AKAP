package playlistMenu.interfaces

import java.io.Serializable


interface ISong {
    val id: Long
    val title: String
    val artist: String
    var duration: Long
    val filePath: String
    var localVolume : Int
    var introDuration : Long
    var outroDuration : Long
}