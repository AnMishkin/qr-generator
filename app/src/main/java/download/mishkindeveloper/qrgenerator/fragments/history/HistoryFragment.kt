package download.mishkindeveloper.qrgenerator.fragments.history

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.MobileAds
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import download.mishkindeveloper.qrgenerator.R
import download.mishkindeveloper.qrgenerator.admob_advanced_native_recyvlerview.AdmobNativeAdAdapter
import download.mishkindeveloper.qrgenerator.databinding.FragmentHistoryBinding
import download.mishkindeveloper.qrgenerator.fragments.globalFunctions.updateOrRequestPermissions
import download.mishkindeveloper.qrgenerator.json.JsonToBase
import download.mishkindeveloper.qrgenerator.model.History
import download.mishkindeveloper.qrgenerator.viewmodels.DatabaseViewModel
import kotlinx.coroutines.InternalCoroutinesApi
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.ArrayList

@InternalCoroutinesApi
class HistoryFragment : Fragment() {

    private lateinit var binding: FragmentHistoryBinding
    private val mDatabaseViewModel: DatabaseViewModel by viewModels()
    private val adapter = HistoryAdapter()
    private var historyList = emptyList<History>()
    private var searchView: SearchView? = null
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private var readPermissionGranted = false
    private var writePermissionGranted = false

    companion object {
        private const val EXPORT_REQUEST_CODE = 1
        private const val IMPORT_REQUEST_CODE = 2
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentHistoryBinding.inflate(inflater, container, false)

        setHasOptionsMenu(true)

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            readPermissionGranted = permissions[android.Manifest.permission.READ_EXTERNAL_STORAGE] ?: readPermissionGranted
            writePermissionGranted = permissions[android.Manifest.permission.WRITE_EXTERNAL_STORAGE] ?: writePermissionGranted
        }
        updateOrRequestPermissions(readPermissionGranted, writePermissionGranted, permissionLauncher)

        //search
//        searchView = binding.root.findViewById(R.id.search)
//        searchView?.clearFocus()

        setupRecyclerView()
        observeHistoryData()
        activity?.let { MobileAds.initialize(it) {} }
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.delete_menu, menu)
        var menuItem = menu?.findItem(R.id.search)
        val searchView = menu.findItem(R.id.search).actionView as SearchView

        searchView?.queryHint = resources.getString(R.string.searchHint)
        val searchItem = menu.findItem(R.id.search)
        if (searchItem != null) {
            val searchView = searchItem.actionView as SearchView?
            val searchTextViewId = searchView!!.context.resources
                .getIdentifier("android:id/search_src_text", null, null)
            val searchTextView = searchView?.findViewById<View>(searchTextViewId) as? TextView
            searchTextView?.setTextColor(Color.WHITE)         // установка цвета текста
             }

        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {

                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter.filter(newText)
                // filterList(newText)
                return true
            }

        })
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.export -> exportFile()
            R.id.imports -> importFile()
            R.id.delete_menu -> deleteAllHistory()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupRecyclerView() {
        val currentAdapter = adapter
        if (currentAdapter != null) {
            val nativeAdId = "ca-app-pub-3971991853344828/9607333997"
            val nativeAdsType = "small" // Замените на "small", "medium" или "custom"
            val interval = 5 // Замените на желаемый интервал повторения рекламы
            val admobNativeAdAdapter = AdmobNativeAdAdapter.Builder
                .with(nativeAdId, currentAdapter, nativeAdsType)
                .adItemIterval(interval)
                .build()
            binding.recyclerView.adapter = admobNativeAdAdapter
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun observeHistoryData() {
        mDatabaseViewModel.readAllData.observe(viewLifecycleOwner, Observer { historyList ->
            adapter.setData(historyList)
        })
    }

    private fun exportFile() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_TITLE, "QR_Generator_base.json")
        }
        startActivityForResult(intent, EXPORT_REQUEST_CODE)
    }

    private fun importFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
        startActivityForResult(intent, IMPORT_REQUEST_CODE)
    }

    fun deleteAllHistory() {
        val yes  = resources.getText(R.string.yes)
        val no  = resources.getText(R.string.no)
        val allHistoryDeleteText  = resources.getText(R.string.all_history_removed)
        val areYouSure  = resources.getText(R.string.are_you_sure)
        val youWantToDeleteAllHistory  = resources.getText(R.string.you_want_delete_all_history)

        val builder = AlertDialog.Builder(requireContext())
        builder.setPositiveButton("$yes"){ _, _ ->
            mDatabaseViewModel.deleteAllHistory()
            // Toast.makeText(requireContext(), "$allHistoryDeleteText", Toast.LENGTH_LONG).show()
            showSnackbar(allHistoryDeleteText as String)
        }

        builder.setNegativeButton("$no"){_, _ -> }
        builder.setTitle(" $areYouSure")
        builder.setMessage(" $youWantToDeleteAllHistory ")
        builder.create().show()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                EXPORT_REQUEST_CODE -> {
                    data?.data?.let { uri ->
                        val outputStream = requireContext().contentResolver.openOutputStream(uri)
                        if (outputStream != null) {
                            val jsonFile = createJsonFile()
                            if (jsonFile != null) {
                                writeFileJson(outputStream as FileOutputStream, jsonFile)
                            }
                        }
                    }
                }
                IMPORT_REQUEST_CODE -> {
                    data?.data?.let { uri ->
                        val inputStream = requireContext().contentResolver.openInputStream(uri)
                        if (inputStream != null) {
                            val fileContent = readFileFromInputStream(inputStream)
                            val importTextFromJson = readFileJson(fileContent)
                            Log.d("MyLog","$importTextFromJson")
                            if (importTextFromJson != null) {
                                when (importTextFromJson) {
                                    "FileNotFoundException" -> {
                                        val message = resources.getText(R.string.message_no_file)
                                        showSnackbar(message.toString())
                                    }
                                    else -> {
                                        val interInBase = Gson().fromJson(
                                            importTextFromJson,
                                            JsonToBase::class.java
                                        )
                                        if (interInBase != null) {
                                            val interInBaseCount = interInBase.size
                                            Log.d(
                                                "MyLog",
                                                "счетчик файла импорта - $interInBaseCount"
                                            )
                                            val oldHistoryList = adapter.giveOldHistoryList()
                                            mDatabaseViewModel.deleteAllHistory()
                                            mDatabaseViewModel.addQrJsonToBase(interInBase)

                                            fun <T> concatenate(vararg lists: List<T>): List<T> {
                                                val result: MutableList<T> = ArrayList()
                                                for (list in lists) {
                                                    result += list
                                                }
                                                return result
                                            }

                                            val list = concatenate(oldHistoryList, interInBase)
                                            mDatabaseViewModel.addListHistory(list)
                                            val message =
                                                resources.getText(R.string.message_import_sucsess)
                                            showSnackbar(message.toString())
                                        }
                                    }
                                }
                        }
                        }else {
                            val message = resources.getText(R.string.message_json_is_null)
                            showSnackbar(message.toString())
                        }
                    }
                }
            }
        }
    }

    private fun createJsonFile(): String? {
        val jsonFile = adapter.toJson()
        val logSize = jsonFile?.length
        if (logSize != null) {
            if (jsonFile.isNullOrEmpty()||logSize<=2){
                val message = resources.getText(R.string.message_json_is_null)
                showSnackbar(message.toString())
                //Toast.makeText(binding.root.context, message, Toast.LENGTH_LONG).show()
                Log.d("MyLog", "длинна json файла - $logSize")
            }
        }
        return jsonFile
    }

    private fun writeFileJson(outputStream: FileOutputStream, jsonFile: String) {
        try {
            outputStream.write(jsonFile.toByteArray())
            outputStream.flush()
            showSnackbar(getString(R.string.message_save_json))
        } catch (e: Exception) {
            showSnackbar(getString(R.string.message_dont_save_json))
        } finally {
            outputStream.close()
        }
    }

    private fun readFileFromInputStream(inputStream: InputStream): String {
        return inputStream.bufferedReader().use { it.readText() }
    }

    private fun readFileJson(fileContent: String): String? {
        val interInBase = Gson().fromJson(fileContent, JsonToBase::class.java)
        if (interInBase == null) {
            val message = resources.getText(R.string.message_no_file)
            showSnackbar(message.toString())
            return null
        }

        val interInBaseCount = interInBase.size
        Log.d("MyLog", "счетчик файла импорта - $interInBaseCount")
        val oldHistoryList = adapter.giveOldHistoryList()
        mDatabaseViewModel.deleteAllHistory()
        mDatabaseViewModel.addQrJsonToBase(interInBase)

        fun <T> concatenate(vararg lists: List<T>): List<T> {
            val result: MutableList<T> = ArrayList()
            for (list in lists) {
                result += list
            }
            return result
        }

        val list = concatenate(oldHistoryList, interInBase)
        mDatabaseViewModel.addListHistory(list)
        val message = resources.getText(R.string.message_import_sucsess)
        showSnackbar(message.toString())

        return fileContent
    }


    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }
}
