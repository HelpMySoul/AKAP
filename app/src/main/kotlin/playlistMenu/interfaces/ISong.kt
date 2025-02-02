package playlistMenu.interfaces

interface ISong {
    val id: Long
    val title: String
    val artist: String
    var duration: Long
    val filePath: String
    var localVolume : Int
    val introDuration : Long
    val outroDuration : Long
}