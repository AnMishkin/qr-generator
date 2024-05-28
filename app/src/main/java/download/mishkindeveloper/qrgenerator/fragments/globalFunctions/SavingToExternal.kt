package download.mishkindeveloper.qrgenerator.fragments.globalFunctions

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.provider.MediaStore
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import download.mishkindeveloper.qrgenerator.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.withContext
import java.io.IOException

@InternalCoroutinesApi
fun Fragment.updateOrRequestPermissions(
    _readPermissionGranted: Boolean,
    _writePermissionGranted: Boolean,
    permissionLauncher: ActivityResultLauncher<Array<String>>) {

    var readPermissionGranted = _readPermissionGranted
    var writePermissionGranted = _writePermissionGranted

    val hasReadPermissions = ContextCompat.checkSelfPermission(
        requireContext(),
        Manifest.permission.READ_EXTERNAL_STORAGE
    ) == PackageManager.PERMISSION_GRANTED

    val hasWritePermissions = ContextCompat.checkSelfPermission(
        requireContext(),
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    ) == PackageManager.PERMISSION_GRANTED

    val minSdk29 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

    readPermissionGranted = hasReadPermissions
    writePermissionGranted = hasWritePermissions || minSdk29

    val permissionToRequest = mutableListOf<String>()
    if (!writePermissionGranted) {
        permissionToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    if (!readPermissionGranted) {
        permissionToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    if(permissionToRequest.isNotEmpty()) {
        permissionLauncher.launch(permissionToRequest.toTypedArray())
    }
}

@InternalCoroutinesApi
suspend fun Fragment.saveQRtoStorage(displayName: String, bmp: Bitmap): Boolean {

    withContext(Dispatchers.IO) {
        val imageCollection = sdk29AndUp {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } ?: MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        val contentValues = ContentValues().apply {
            sdk29AndUp {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/QR Generator")
            }
            put(MediaStore.Images.Media.MIME_TYPE, "Image/Jpeg")
            put(MediaStore.Images.Media.WIDTH, bmp.width)
            put(MediaStore.Images.Media.HEIGHT, bmp.height)
            put(MediaStore.Images.ImageColumns.DISPLAY_NAME, displayName)
        }

        return@withContext try {
            val could = resources.getText(R.string.could_save_bitmap)
            val couldShare = resources.getText(R.string.failed_to_share)
            requireContext().contentResolver.insert(imageCollection, contentValues)?.also { uri ->
                requireContext().contentResolver.openOutputStream(uri).use { outputStream ->
                    if (!bmp.compress(Bitmap.CompressFormat.JPEG, 100, outputStream!!)) {
                        throw IOException("$could")
                    }
                }
            } ?: throw IOException("$couldShare")
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }
    return false
}

