package download.mishkindeveloper.qrgenerator.fragments.success

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import download.mishkindeveloper.qrgenerator.R
import download.mishkindeveloper.qrgenerator.databinding.FragmentSuccessBinding
import download.mishkindeveloper.qrgenerator.fragments.globalFunctions.*
import download.mishkindeveloper.qrgenerator.model.History
import download.mishkindeveloper.qrgenerator.viewmodels.DatabaseViewModel
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.launch
import java.util.*


@InternalCoroutinesApi
class SuccessFragment : Fragment() {
    private lateinit var mAdView : AdView

    private var isImageScaled = false
    private lateinit var binding: FragmentSuccessBinding
    private val mDatabaseViewModel: DatabaseViewModel by viewModels()
    private val args:SuccessFragmentArgs by navArgs()
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private var readPermissionGranted = false
    private var writePermissionGranted = false



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
       binding =  FragmentSuccessBinding.inflate(inflater, container, false)


        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            readPermissionGranted = permissions[Manifest.permission.READ_EXTERNAL_STORAGE] ?: readPermissionGranted
            writePermissionGranted = permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] ?: writePermissionGranted
        }
        updateOrRequestPermissions(readPermissionGranted, writePermissionGranted, permissionLauncher)


        val bitmap = generateQr(args.qrText)
        binding.imageView.setImageBitmap(bitmap)
        binding.nameQrSuccess.text = args.qrType
        insertDataToDatabase(bitmap)

        //увеличение картинки
        binding.imageView.setOnClickListener { v ->
            if (!isImageScaled) {
                v.animate().scaleX(1.4f).scaleY(1.4f).setDuration(500).translationY(300f)
                binding.shareQR.isVisible = false
                binding.saveQR.isVisible = false
                binding.textView.isVisible = false
                binding.nameQrSuccess.isVisible = false
                binding.adView.isVisible = false
            }
            if (isImageScaled) {
                R.style.Theme_AppCompat_DayNight_NoActionBar
                v.animate().scaleX(1f).scaleY(1f).setDuration(500).translationY(-40f)
                binding.shareQR.isVisible = true
                binding.saveQR.isVisible = true
                binding.textView.isVisible = true
                binding.nameQrSuccess.isVisible = true
                binding.adView.isVisible = true
            }
            isImageScaled = !isImageScaled
        }


        binding.shareQR.setOnClickListener {
            shareQr(bitmap, requireContext())
        }

        binding.saveQR.setOnClickListener {
            val savedOk = resources.getText(R.string.photo_saved_ok)
            val failedSavedPhoto = resources.getText(R.string.failed_save_photo)
            lifecycleScope.launch {
               if (!writePermissionGranted) {
                    saveQRtoStorage(UUID.randomUUID().toString(), bitmap)
                    showToast("$savedOk", requireContext())
                } else {
                    showToast("$failedSavedPhoto", requireContext())
                }
            }
        }

        //банерная реклама
        MobileAds.initialize(this.requireContext()) {}
        mAdView = binding.adView
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
        //конец банерная реклама

        return binding.root
    }





    private fun insertDataToDatabase(bmp: Bitmap) {
        val text = args.text
        val qrText = args.qrText
        val insertType = args.qrType
        val addNameQr = args.text

        if (inputCheck(text)) {
            lifecycleScope.launch {
                val history = History(text,addNameQr, qrText,  insertType, bmp)
                mDatabaseViewModel.addQrHistory(history)
            }
        }
    }


    private fun inputCheck(text: String): Boolean {
        return !(TextUtils.isEmpty(text))
    }




}