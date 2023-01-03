package download.mishkindeveloper.qrgenerator.fragments.qr

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import download.mishkindeveloper.qrgenerator.R
import download.mishkindeveloper.qrgenerator.databinding.FragmentQrBinding
import download.mishkindeveloper.qrgenerator.fragments.globalFunctions.generateQr
import download.mishkindeveloper.qrgenerator.fragments.globalFunctions.saveQRtoStorage
import download.mishkindeveloper.qrgenerator.fragments.globalFunctions.shareQr
import download.mishkindeveloper.qrgenerator.fragments.globalFunctions.showToast
import download.mishkindeveloper.qrgenerator.fragments.history.HistoryAdapter
import download.mishkindeveloper.qrgenerator.viewmodels.DatabaseViewModel
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.launch
import java.util.*

@InternalCoroutinesApi
class QrFragment : Fragment() {
    private val adapterHistory = HistoryAdapter()
    private var isImageScaled = false
    private lateinit var binding: FragmentQrBinding
    private val mDatabaseViewModel: DatabaseViewModel by viewModels()
    private val args by navArgs<QrFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =  FragmentQrBinding.inflate(inflater, container, false)

        //увеличение картинки
        binding.imageView.setOnClickListener { v ->
            if (!isImageScaled) {
                v.animate().scaleX(1.4f).scaleY(1.4f).setDuration(500).translationY(300f)
                binding.shareQR.isVisible = false
                binding.saveQR.isVisible = false
                binding.type.isVisible = false
                binding.qrGeneratedText.isVisible = false

            }
            if (isImageScaled) {
                R.style.Theme_AppCompat_DayNight_NoActionBar
               v.animate().scaleX(1f).scaleY(1f).setDuration(500).translationY(-40f)
                binding.shareQR.isVisible = true
                binding.saveQR.isVisible = true
                binding.type.isVisible = true
                binding.qrGeneratedText.isVisible = true

            }
            isImageScaled = !isImageScaled
        }

        binding.type.text = args.currentQR.type
        textFigureOut(binding, args)

        lifecycleScope.launch {
            val bmp =  generateQr(args.currentQR.qrText)
            binding.imageView.setImageBitmap(bmp)

            binding.shareQR.setOnClickListener {
                shareQr(bmp, requireContext())
            }
        }



        setHasOptionsMenu(true)

        binding.saveQR.setOnClickListener {
            val photoSavedOk  = resources.getText(R.string.photo_saved_ok)
            val failedSavePhoto  = resources.getText(R.string.failed_save_photo)
            var writePermissionGranted = false
            val bmp =  generateQr(args.currentQR.qrText)
            val nameQr = binding.type.text.toString()
            lifecycleScope.launch {
                if (!writePermissionGranted) {
                    saveQRtoStorage(nameQr, bmp)
                    showToast("$photoSavedOk", requireContext())
                } else {
                    showToast("$failedSavePhoto", requireContext())
                }
            }
        }
        return  binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.delete_menu,menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.delete_menu) {
            deleteQrHistory()
        }
        return super.onOptionsItemSelected(item)
    }

    fun deleteQrHistory() {
        val yes  = resources.getText(R.string.yes)
        val no  = resources.getText(R.string.no)
        val removed  = resources.getText(R.string.removed)
        val delete  = resources.getText(R.string.delete)
        val sure  = resources.getText(R.string.sure_delete_qr)
        val builder = AlertDialog.Builder(requireContext())
        builder.setPositiveButton("$yes"){ _, _ ->
            mDatabaseViewModel.deleteQrHistory(args.currentQR)
//adapterHistory.notifyDataSetChanged()
            Toast.makeText(requireContext(), "$removed ${args.currentQR.type}", Toast.LENGTH_SHORT).show()
        findNavController().navigateUp()

        }
        builder.setNegativeButton("$no"){_, _ -> }
        builder.setTitle("$delete ${args.currentQR.type} ?")
        builder.setMessage("$sure")
        builder.create().show()
    }


}