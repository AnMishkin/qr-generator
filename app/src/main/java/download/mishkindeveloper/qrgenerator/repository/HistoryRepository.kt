package download.mishkindeveloper.qrgenerator.repository

import androidx.lifecycle.LiveData
import download.mishkindeveloper.qrgenerator.data.HistoryDao
import download.mishkindeveloper.qrgenerator.json.JsonToBase
import download.mishkindeveloper.qrgenerator.model.History
import kotlinx.coroutines.Dispatchers


class HistoryRepository(private val historyDao: HistoryDao) {

    val readAllData: LiveData<List<History>> = historyDao.readAllData()
    //val readAllDataForJson: MutableList<History> = historyDao.readAllDataForJson()

    suspend fun addQrHistory(history:History) {
        historyDao.addQrHistory(history)
    }
    suspend fun addListHistory(history: List<History>) {
        historyDao.addListHistory(history)
    }
    suspend fun addQrJsonToBase(jsonToBase: JsonToBase) {
        historyDao.addQrJsonToBase(jsonToBase)
    }

    suspend fun addNameQr(history: History){
        historyDao.addNameQr(history)
    }

    suspend fun deleteQrHistory(history: History) {
        historyDao.deleteQrHistory(history)
    }

    suspend fun deleteAllHistory() {
        historyDao.deleteAllHistory()
    }

    suspend fun updateQr(history: History) {
        historyDao.update(history)
    }
}