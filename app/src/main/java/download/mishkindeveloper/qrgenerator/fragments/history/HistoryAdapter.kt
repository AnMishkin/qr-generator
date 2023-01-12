package download.mishkindeveloper.qrgenerator.fragments.history

import android.os.Environment
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import download.mishkindeveloper.qrgenerator.R
import download.mishkindeveloper.qrgenerator.databinding.CustomRowBinding
import download.mishkindeveloper.qrgenerator.databinding.FragmentHistoryBinding
import download.mishkindeveloper.qrgenerator.databinding.FragmentHomeBinding
import download.mishkindeveloper.qrgenerator.model.History
import kotlinx.coroutines.InternalCoroutinesApi
import java.io.*



class HistoryAdapter: RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    val mapper = jacksonObjectMapper()
    private var historyList = emptyList<History>()

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


        fun bind(history: History) {
            text.text = history.text
            type.text = history.type
            //qrBitmap.load(history.qr)
            //qrBitmap.setBackgroundColor(R.drawable.ic_qr_icon)
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
    fun HistoryFragment.showSnackBar(binding: FragmentHomeBinding ,message: String) {
        val snackBar = Snackbar.make(
            binding.rootLayout,
            message,
            Snackbar.LENGTH_SHORT
        )
        snackBar.setAction("Ok"){}
        snackBar.setActionTextColor(ContextCompat.getColor(requireContext(), R.color.teal_200))
        snackBar.setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.blue))
        snackBar.show()
    }



    fun readFileJson(filePath: String, fileName: String): String {
        // Аналогично создается объект файла
        val myFile = File(Environment.getExternalStorageDirectory().toString() + "/" + filePath + fileName)
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





}


