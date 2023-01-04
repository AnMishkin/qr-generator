package download.mishkindeveloper.qrgenerator.fragments.history

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import coil.load
import download.mishkindeveloper.qrgenerator.databinding.CustomRowBinding
import download.mishkindeveloper.qrgenerator.model.History
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

class HistoryAdapter: RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    val mapper = jacksonObjectMapper()
    private var historyList = emptyList<History>()


    class ViewHolder(binding: CustomRowBinding) : RecyclerView.ViewHolder(binding.root) {
        private val qrBitmap: ImageView = binding.QrBitmap
        private val type = binding.type
        private val text = binding.text
        private val nameQr = binding.nameQr

        private val delOneQr = binding.imDelete


        var rowLayout: ConstraintLayout = binding.rowLayout


        fun bind(history: History) {
            text.text = history.text
            type.text = history.type
            qrBitmap.load(history.qr)
            nameQr.text = history.addNameQr

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            CustomRowBinding
                .inflate
                    (
                    LayoutInflater.from(parent.context), parent, false
                )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = historyList[position]
        holder.bind(currentItem)

        holder.rowLayout.setOnClickListener {
            val action = HistoryFragmentDirections.actionHistoryFragmentToQrFragment(currentItem)
            holder.itemView.findNavController().navigate(action)
        }


    }

    override fun getItemCount(): Int {
        return historyList.size
    }

    fun setData(history: List<History>) {
        this.historyList = history
        notifyDataSetChanged()
    }

//    fun JsonPerse(history: List<History>){
//        this.historyList = history
//        var OutJson:<String>()
//        var exportQr = JSONObject(
//            this,OutJson,
//        )
//    }
//
//    interface Listener{
//        fun deleteOneQr()
//    }

//    fun jsonParse(){
//        val historyForParseList = mutableListOf<History>()
//        historyForParseList.addAll(historyList)
//
//        Log.d("MyLog","$historyForParseList")
//
//    }

}

//private fun toJson(){
//    val historyDao = HistoryDao()
//    val readAllData: LiveData<List<History>> = historyDao.readAllData()
//    val to =
//    Log.d("MyLog","$to")
//    return
//
//}


 fun toJson(){
//    val historyToJson : MutableList<History> = MutableList<History>()
////qrListToJson()
//    Log.d("MyLog","$historyToJson")
}



