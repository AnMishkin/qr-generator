package download.mishkindeveloper.qrgenerator.converters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.TypeConverter
import download.mishkindeveloper.qrgenerator.model.History
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream

class Converters {

    @TypeConverter
    fun fromBitmap(bitmap: Bitmap): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 5, outputStream)
        return outputStream.toByteArray()
    }

    @TypeConverter
    fun toBitmap(byteArray: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }


        @TypeConverter
        fun fromAppToJsonList(value : MutableList<History>) = Json.encodeToString(value)

        @TypeConverter
        fun toAppFromJsonList(value: String) = Json.decodeFromString<MutableList<History>>(value)



}