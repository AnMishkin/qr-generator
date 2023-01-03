package download.mishkindeveloper.qrgenerator.repository

import androidx.lifecycle.LiveData
import download.mishkindeveloper.qrgenerator.data.HistoryDao
import download.mishkindeveloper.qrgenerator.model.History

class HistoryRepository(private val historyDao: HistoryDao) {

    val readAllData: LiveData<List<History>> = historyDao.readAllData()
    //val readAllDataForJson: MutableList<History> = historyDao.readAllDataForJson()

    suspend fun addQrHistory(history: History) {
        historyDao.addQrHistory(history)
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

//    suspend fun readAllDataForJson() {
//        historyDao.readAllDataForJson()
//    }
}