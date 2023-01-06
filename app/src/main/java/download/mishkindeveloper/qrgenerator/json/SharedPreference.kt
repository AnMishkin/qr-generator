package download.mishkindeveloper.qrgenerator.json
import android.content.Context
import android.content.SharedPreferences

class SharedPreference(val context: Context) {
    private val COUNT_AD = "kotlincodes"
    private val sharedPref: SharedPreferences = context.getSharedPreferences(COUNT_AD, Context.MODE_PRIVATE)

    fun saveSharedCountAd(KEY_NAME: String, value: Int) {
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putInt(KEY_NAME, value)
        editor.commit()
    }

    fun getSharedCountAd(KEY_NAME: String): Int {
        return sharedPref.getInt(KEY_NAME, 0)
    }


}