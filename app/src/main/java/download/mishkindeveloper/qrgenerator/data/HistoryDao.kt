package download.mishkindeveloper.qrgenerator.data

import androidx.lifecycle.LiveData
import androidx.room.*
import download.mishkindeveloper.qrgenerator.model.History


@Dao
interface HistoryDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addQrHistory(history: History)

    @Insert
    suspend fun addNameQr(history: History)

    @Delete
    suspend fun deleteQrHistory(history: History)

    @Query("DELETE FROM history_table")
    suspend fun deleteAllHistory()

    @Query("SELECT * FROM history_table ORDER BY id DESC")
    fun readAllData(): LiveData<List<History>>

//    @Query("SELECT * FROM history_table ORDER BY id ASC")
//    fun readAllDataForJson(): MutableList<History>

}