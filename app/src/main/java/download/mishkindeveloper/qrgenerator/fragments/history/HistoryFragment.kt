package download.mishkindeveloper.qrgenerator.fragments.history

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.view.isEmpty
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import download.mishkindeveloper.qrgenerator.R
import download.mishkindeveloper.qrgenerator.databinding.FragmentHistoryBinding
import download.mishkindeveloper.qrgenerator.fragments.globalFunctions.showToast
import download.mishkindeveloper.qrgenerator.fragments.qr.QrFragmentArgs
import download.mishkindeveloper.qrgenerator.viewmodels.DatabaseViewModel
import kotlinx.coroutines.InternalCoroutinesApi
import download.mishkindeveloper.qrgenerator.model.History
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.android.gms.ads.MobileAds
import com.google.gson.Gson

@InternalCoroutinesApi
class HistoryFragment : Fragment() {

    private lateinit var binding: FragmentHistoryBinding
    private val mDatabaseViewModel: DatabaseViewModel by viewModels()
    private val adapter = HistoryAdapter()
    private val mapper = jacksonObjectMapper()
    private var historyList = emptyList<History>()
    private val args by navArgs<QrFragmentArgs>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHistoryBinding.inflate(inflater , container, false)
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
                showToast("$createFirst", requireContext())
            } else {
                deleteAllHistory()

            }
        }

        //экспорт json файла
        if (item.itemId==R.id.export){
adapter.toJson()
        }

        return super.onOptionsItemSelected(item)
    }

    private fun deleteAllHistory() {
        val yes  = resources.getText(R.string.yes)
        val no  = resources.getText(R.string.no)
        val allHistoryDeleteText  = resources.getText(R.string.all_history_removed)
        val areYouSure  = resources.getText(R.string.are_you_sure)
        val youWantToDeleteAllHistory  = resources.getText(R.string.you_want_delete_all_history)

        val builder = AlertDialog.Builder(requireContext())
        builder.setPositiveButton("$yes"){ _, _ ->
            mDatabaseViewModel.deleteAllHistory()
            Toast.makeText(requireContext(), "$allHistoryDeleteText", Toast.LENGTH_SHORT).show()

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



}