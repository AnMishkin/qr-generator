package download.mishkindeveloper.qrgenerator.json

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import download.mishkindeveloper.qrgenerator.model.History
import download.mishkindeveloper.qrgenerator.fragments.history.HistoryFragment
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject


//@kotlinx.serialization.Serializable
//class QrToJson(baseToJson: LiveData<List<History>>) {
//fun toJson(history: LiveData<List<History>>){
//    val base = history
//    val toJson = Json.encodeToString(base)
//    Log.d("MyLog","$toJson")
//}
//
//
//
//
//}
//@OptIn(InternalCoroutinesApi::class)
//val history = HistoryFragment()
//
//    private var historyList = emptyList<History>()

//val historyToJson =
