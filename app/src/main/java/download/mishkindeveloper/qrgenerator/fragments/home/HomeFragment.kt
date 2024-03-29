package download.mishkindeveloper.qrgenerator.fragments.home

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import download.mishkindeveloper.qrgenerator.R
import download.mishkindeveloper.qrgenerator.databinding.FragmentHomeBinding
import kotlinx.coroutines.launch


class HomeFragment : Fragment() {
    private lateinit var mAdView: AdView
    private lateinit var binding: FragmentHomeBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        //банерная реклама
        MobileAds.initialize(requireContext())
        mAdView = binding.adViewHome

        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        setHasOptionsMenu(true)
val editText = binding.plainText

        var home = R.layout.fragment_home

//        //банерная реклама
//        MobileAds.initialize(this.requireContext()) {}
//        mAdView = binding.adView
//        val adRequest = AdRequest.Builder().build()
//        mAdView.loadAd(adRequest)
//        //конец банерная реклама

        linkAndTextRequest(binding)
            binding.clear.setOnClickListener {
                clearText(binding)
            }




        return binding.root


    }

    override fun onDestroy() {
        mAdView.destroy()
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        instagramRequest(binding)
        facebookRequest(binding)
        linkAndTextRequest(binding)
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
         inflater.inflate(R.menu.home_menu,menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.home_menu) {
            //создание кода
            onGenerateClicked()
           if (binding.plainText.text.isNotEmpty()){
               item.isVisible = false
           }
        }

        return true
    }


    // this function triggered when the generate button is clicked
    private fun onGenerateClicked() {
        val fillBoxesText  = resources.getText(R.string.fill_boxes)
        lifecycleScope.launch {
            if(binding.plainText.text.isEmpty()
                ||binding.edNameQr.text.isEmpty()
            )  {
                showSnackBar(binding, "$fillBoxesText")
            }else {
                applyAnimations(binding)


                navigateToSuccess()



            }
        }
    }

    // this function triggered when navigate to next fragment
    private fun navigateToSuccess() {
        var qrText = binding.plainText.text.toString()
        val text = qrText
        val qrType: String
        var addNameQr = binding.edNameQr.text.toString()

        when {
            binding.instagram.isActivated -> {
                qrText = "instagram://user?username=$qrText"
                qrType = binding.instagram.tag.toString()
                addNameQr = binding.edNameQr.text.toString()
            }
            binding.facebook.isActivated -> {
                qrText = "fb://profile/$qrText"
                qrType = binding.facebook.tag.toString()
                addNameQr = binding.edNameQr.text.toString()
            }
            else -> {
                qrText = binding.plainText.text.toString()
                qrType = binding.linkText.tag.toString()
                addNameQr = binding.edNameQr.text.toString()
            }
        }
        val direction = HomeFragmentDirections.actionHomeFragmentToSuccessFragment(
            text,
            addNameQr,
            //qrType,
            qrText,
            //countAd
            //mArgs
        )
        findNavController().navigate(direction)
        binding.edNameQr.text.clear()
        binding.plainText.text.clear()
    }

}