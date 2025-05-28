package player.interfaces


interface ISong {
    var id:            Long
    var duration:      Long
    var introDuration: Long
    var outroDuration: Long

    val title:         String
    val artist:        String
    val filePath:      String

    var localVolume:   Int
}