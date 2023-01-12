package download.mishkindeveloper.qrgenerator.json

import androidx.room.PrimaryKey

data class JsonToBaseItem(
    val addNameQr: String,
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    val qrText: String,
    val text: String,
    val type: String
)