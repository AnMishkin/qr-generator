package download.mishkindeveloper.qrgenerator.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import download.mishkindeveloper.qrgenerator.data.HistoryDatabase
import download.mishkindeveloper.qrgenerator.json.JsonToBase
import download.mishkindeveloper.qrgenerator.model.History
import download.mishkindeveloper.qrgenerator.repository.HistoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.launch

@InternalCoroutinesApi
class DatabaseViewModel(application: Application): AndroidViewModel(application) {

   val readAllData: LiveData<List<History>>
   private val repository: HistoryRepository


   init {
       val historyDao = HistoryDatabase.getDatabase(application).historyDao()
       repository = HistoryRepository(historyDao)
       readAllData= repository.readAllData
   }

    fun addNameQr(history: History){
        viewModelScope.launch(Dispatchers.IO) {
            repository.addNameQr(history)
        }
    }

    fun addQrHistory(history: History) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addQrHistory(history)
        }
    }
    fun addListHistory(history: List<History>) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addListHistory(history)
        }
    }

    fun addQrJsonToBase(jsonToBase: JsonToBase) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addQrJsonToBase(jsonToBase)
        }
    }

    fun deleteQrHistory(history: History) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteQrHistory(history)
        }
    }

    fun deleteAllHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllHistory()
        }
    }

//    fun readAllDataForJson() {
//        viewModelScope.launch(Dispatchers.IO) {
//            repository.readAllDataForJson()
//        }
//    }

}