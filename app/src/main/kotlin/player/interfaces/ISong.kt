package player.interfaces


interface ISong {
    var id:            Long
    var duration:      Long
    var introDuration: Long
    var outroDuration: Long

    var title:         String
    var artist:        String
    val filePath:      String

    var localVolume:   Int
}