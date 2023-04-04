package download.mishkindeveloper.qrgenerator.fragments.history

import android.Manifest
import android.app.AlertDialog
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.*
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isEmpty
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import download.mishkindeveloper.qrgenerator.R
import download.mishkindeveloper.qrgenerator.databinding.FragmentHistoryBinding
import download.mishkindeveloper.qrgenerator.databinding.FragmentHomeBinding
import download.mishkindeveloper.qrgenerator.fragments.globalFunctions.showToast
import download.mishkindeveloper.qrgenerator.fragments.globalFunctions.updateOrRequestPermissions
import download.mishkindeveloper.qrgenerator.fragments.qr.QrFragmentArgs
import download.mishkindeveloper.qrgenerator.json.JsonToBase
import download.mishkindeveloper.qrgenerator.model.History
import download.mishkindeveloper.qrgenerator.viewmodels.DatabaseViewModel
import kotlinx.coroutines.InternalCoroutinesApi
import java.io.File
import java.io.FileOutputStream


@InternalCoroutinesApi
class HistoryFragment : Fragment() {

    private lateinit var binding: FragmentHistoryBinding
    private val mDatabaseViewModel: DatabaseViewModel by viewModels()
    private val adapter = HistoryAdapter()
    private val mapper = jacksonObjectMapper()
    private var historyList = emptyList<History>()
    private val args by navArgs<QrFragmentArgs>()
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private var readPermissionGranted = false
    private var writePermissionGranted = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHistoryBinding.inflate(inflater , container, false)


        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            readPermissionGranted = permissions[Manifest.permission.READ_EXTERNAL_STORAGE] ?: readPermissionGranted
            writePermissionGranted = permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] ?: writePermissionGranted
        }
        updateOrRequestPermissions(readPermissionGranted, writePermissionGranted, permissionLauncher)



startHistoryView()

allHistoryInList()

        setHasOptionsMenu(true)
        return binding.root


    }

    private fun allHistoryInList(){
        mDatabaseViewModel.readAllData.observe(viewLifecycleOwner, Observer {
            adapter.setData(it)
        })
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        inflater.inflate(R.menu.delete_menu,menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val createFirst = resources.getText(R.string.create_qr_first)
        val recyclerViewEmpty = binding.recyclerView.isEmpty()
        if (item.itemId == R.id.delete_menu) {
            if (recyclerViewEmpty) {
                showSnackBar(binding,"$createFirst")
                //showToast("$createFirst", requireContext())
            } else {
                deleteAllHistory()

            }
        }

        //экспорт json файла
        if (item.itemId==R.id.export){
//adapter.toJson()
            val jsonFile = adapter.toJson()
            val logSize = jsonFile?.length
            if (logSize != null) {
                if (jsonFile.isNullOrEmpty()||logSize<=2){
                    val message = resources.getText(R.string.message_json_is_null)
                    showSnackBar(binding,message.toString())
                    //Toast.makeText(binding.root.context, message, Toast.LENGTH_LONG).show()
                    Log.d("MyLog", "длинна json файла - $logSize")
                } else{

                    writeFileJson("download/","QR Generator Base.json", jsonFile)
                }
            }


        }

        //импорт json файла
        if (item.itemId==R.id.imports){

            val importTextFromJson = adapter.readFileJson("download/", "QR Generator base.json")

when (importTextFromJson){
    "FileNotFoundException" ->{
        val message = resources.getText(R.string.message_no_file)
        //Toast.makeText(binding.root.context, message, Toast.LENGTH_LONG).show()
        showSnackBar(binding,message.toString())
    }
    else -> {
        val interInBase = Gson().fromJson(importTextFromJson,JsonToBase::class.java)
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
        showSnackBar(binding,message.toString())
    }
}


        }

        return super.onOptionsItemSelected(item)
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
            showSnackBar(binding,allHistoryDeleteText.toString())
        }

        builder.setNegativeButton("$no"){_, _ -> }
        builder.setTitle(" $areYouSure")
        builder.setMessage(" $youWantToDeleteAllHistory ")
        builder.create().show()
    }

     fun startHistoryView(){
        val recyclerView = binding.recyclerView
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    fun writeFileJson(filePath: String, fileName: String, text: String) {

        try {
            val myFile = File(Environment.getExternalStorageDirectory().toString() + "/" + filePath + fileName)
            myFile.createNewFile()
            val outputStream = FileOutputStream(myFile)
            outputStream.write(text.toByteArray())

            val message = resources.getText(R.string.message_save_json)
            //Toast.makeText(binding.root.context, message, Toast.LENGTH_LONG).show()
            showSnackBar(binding,message.toString())
        } catch (e: Exception) { 
            e.printStackTrace()
            val message = resources.getText(R.string.message_dont_save_json)
            //Toast.makeText(binding.root.context, message, Toast.LENGTH_LONG).show()
            showSnackBar(binding,message.toString())
        }
    }

    @OptIn(InternalCoroutinesApi::class)
    fun HistoryFragment.showSnackBar(binding: FragmentHistoryBinding, message: String) {
        val snackBar = Snackbar.make(
            binding.root,
            message,
            Snackbar.LENGTH_SHORT
        )
        snackBar.setAction("Ok") {}
        snackBar.setActionTextColor(ContextCompat.getColor(requireContext(), R.color.teal_200))
        snackBar.setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.blue))
        snackBar.show()
    }
}