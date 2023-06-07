package download.mishkindeveloper.qrgenerator.model

import android.graphics.Bitmap
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
@Entity(tableName = "history_table")
data class History (
    @ColumnInfo(name = "text") var text: String,
    @ColumnInfo(name = "addNameQr") var addNameQr: String,
    @ColumnInfo(name = "qr_text") var qrText: String,
    @ColumnInfo(name = "type") var type: String,
    //@ColumnInfo(name = "QR") val qr: Bitmap
        ): Parcelable {


      @IgnoredOnParcel
      @PrimaryKey(autoGenerate = true)
          var id: Int = 0
        }
