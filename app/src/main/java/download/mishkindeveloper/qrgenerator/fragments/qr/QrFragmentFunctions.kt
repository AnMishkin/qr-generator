package download.mishkindeveloper.qrgenerator.fragments.qr

import download.mishkindeveloper.qrgenerator.databinding.FragmentQrBinding
import kotlinx.coroutines.InternalCoroutinesApi

@InternalCoroutinesApi
fun QrFragment.textFigureOut(binding: FragmentQrBinding, args: QrFragmentArgs) {
    val inputText = args.currentQR.text
    val instagramHandler = "Username : @$inputText"
    val facebookHandler = "Facebook : $inputText"
   // val textOrLinkHandler = "Text or Link : $inputText"
    val textOrLinkHandler = "$inputText"


    when (args.currentQR.type) {
        "Instagram" -> {
            binding.qrGeneratedText.text = instagramHandler
        }

        "Facebook" -> {
            binding.qrGeneratedText.text = facebookHandler
        }

        else -> {
            binding.qrGeneratedText.text = textOrLinkHandler
        }
    }
}