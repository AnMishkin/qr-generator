package download.mishkindeveloper.qrgenerator.fragments.history

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import download.mishkindeveloper.qrgenerator.MainActivity
import download.mishkindeveloper.qrgenerator.R
import download.mishkindeveloper.qrgenerator.databinding.CustomRowBinding
import download.mishkindeveloper.qrgenerator.databinding.FragmentHistoryBinding
import download.mishkindeveloper.qrgenerator.databinding.FragmentHomeBinding
import download.mishkindeveloper.qrgenerator.fragments.qr.AdmobNativeAdAdapterListener
import download.mishkindeveloper.qrgenerator.fragments.qr.QrFragment
import download.mishkindeveloper.qrgenerator.model.History
import download.mishkindeveloper.qrgenerator.moveItemRecycler.ItemTouchHelperAdapter
import kotlinx.coroutines.InternalCoroutinesApi
import java.io.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.coroutines.coroutineContext



class HistoryAdapter @OptIn(InternalCoroutinesApi::class) constructor(private val historyFragment: HistoryFragment): RecyclerView.Adapter<HistoryAdapter.ViewHolder>(), Filterable,ItemTouchHelperAdapter,
    AdmobNativeAdAdapterListener {

    val mapper = jacksonObjectMapper()
    private var historyList = emptyList<History>()
    private var historyListFiltered = emptyList<History>()

    //для сохранения json файла
    private var fileName: String? = "QR Generator base.json"
    private val filePath = "MyFileStorage"

    lateinit var binding: FragmentHistoryBinding

    private var mFile: File? = null
    private val mData = ""

    class ViewHolder(binding: CustomRowBinding) : RecyclerView.ViewHolder(binding.root) {
        private val qrBitmap: ImageView = binding.QrBitmap
        private val type = binding.type
        private val text = binding.text
        private val nameQr = binding.nameQr
        private val delOneQr = binding.imDelete


        var rowLayout: ConstraintLayout = binding.rowLayout
        var imEditInHistory : ImageButton = binding.imEditButton

        fun bind(history: History) {
            text.text = history.text
            type.text = history.type
            //qrBitmap.load(history.qr)
            //qrBitmap.setBackgroundColor(R.drawable.ic_qr_icon)
            nameQr.text = history.addNameQr

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        //activity = parent.context as Activity
        //initAds()
        return ViewHolder(
            CustomRowBinding
                .inflate
                    (
                    LayoutInflater.from(parent.context), parent, false
                )
        )
    }

    @OptIn(InternalCoroutinesApi::class)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = historyList[position]
        holder.bind(currentItem)

        //редактирование QR кода в истории
        holder.imEditInHistory.setOnClickListener {
            historyFragment.editOneHistory(currentItem, holder.itemView.context)
            Log.d("MyLog","Hello")

        }

        holder.rowLayout.setOnClickListener {
            val action = HistoryFragmentDirections.actionHistoryFragmentToQrFragment(currentItem)
            holder.itemView.findNavController().navigate(action)
            val qrFragment = QrFragment()
            qrFragment.admobNativeAdAdapterListener = this

        }
    }


    override fun getItemCount(): Int {
        return historyList.size
    }

    fun setData(history: List<History>) {
        this.historyList = history
        this.historyListFiltered = history
        notifyDataSetChanged()
    }


    fun toJson(): String? {
        val allData = historyList
        val json = Gson().toJson(allData)
        return json
    }

    fun giveOldHistoryList(): List<History> {

        val allData = historyList
        return historyList
    }


    // Записать файл

    @OptIn(InternalCoroutinesApi::class)
    fun HistoryFragment.showSnackBar(binding: FragmentHomeBinding, message: String) {
        val snackBar = Snackbar.make(
            binding.rootLayout,
            message,
            Snackbar.LENGTH_SHORT
        )
        snackBar.setAction("Ok") {}
        snackBar.setActionTextColor(ContextCompat.getColor(requireContext(), R.color.teal_200))
        snackBar.setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.blue))
        snackBar.show()
    }


    fun readFileJson(filePath: String, fileName: String): String {
        // Аналогично создается объект файла
        val myFile =
            File(Environment.getExternalStorageDirectory().toString() + "/" + filePath + fileName)
        try {
            val inputStream = FileInputStream(myFile)

            // Буферизируем данные из выходного потока файла
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
            // Класс для создания строк из последовательностей символов
            val stringBuilder = StringBuilder()
            var line: String?
            try {
                // Производим построчное считывание данных из файла в конструктор строки,
                while (bufferedReader.readLine().also { line = it } != null) {
                    stringBuilder.append(line)
                }
                return stringBuilder.toString()
            } catch (e: IOException) {
                e.printStackTrace()
                return ""
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            return "FileNotFoundException"
        }
    }

    override fun getFilter(): Filter {
        var filter = object : Filter() {
            override fun performFiltering(p0: CharSequence?): FilterResults {
                val filterResults = FilterResults()
                if (p0 == null || p0.isEmpty()) {
                    filterResults.values = historyListFiltered
                    filterResults.count = historyListFiltered.size
                } else {
                    var searchChar = p0.toString().lowercase()
                    var filteredResults = ArrayList<History>()

                    for (item in historyListFiltered) {
                        if (item.type.lowercase().contains(searchChar)){
                            filteredResults.add(item)
                        }
                    }
                    filterResults.values = filteredResults
                    filterResults.count = filteredResults.size
                }
                return filterResults
            }

            override fun publishResults(p0: CharSequence?, p1: FilterResults?) {
                historyList = p1!!.values as List<History>
                notifyDataSetChanged()
            }

        }

return filter
    }

//    override fun onItemDismiss(position: Int) {
//        historyList = historyList.filterIndexed { index, _ -> index != position }
//        notifyItemRemoved(position)
//    }

    override fun onItemMove(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(historyList, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(historyList, i, i - 1)
            }
        }
        notifyItemMoved(fromPosition, toPosition)
    }

    fun updateList(newList: List<History>) {
        historyList = newList
        notifyDataSetChanged()
    }


//    fun clickDelete() {
//        //редактирование но не тут
//        val editTextQr: ImageButton = binding.root.findViewById(R.id.imEditButton)
//        editTextQr.setOnClickListener {
//            Log.d("MyLog", "Нажали редактировать QR")
//        }
//    }

}
