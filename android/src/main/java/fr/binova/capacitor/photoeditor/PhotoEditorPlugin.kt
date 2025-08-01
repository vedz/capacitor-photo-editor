package fr.binova.capacitor.photoeditor

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import com.getcapacitor.JSObject
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.annotation.CapacitorPlugin

@CapacitorPlugin(name = "PhotoEditor")
class PhotoEditorPlugin : Plugin() {
    private var launcher: ActivityResultLauncher<Intent?>? = null
    private var savedCall: PluginCall? = null

    override fun load() {
        launcher = bridge.registerForActivityResult(
            StartActivityForResult()
        ) { result: ActivityResult ->
            savedCall?.let { call ->
                if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                    val editedImagePath = result.data?.getStringExtra("editedImagePath")
                    val ret = JSObject().apply {
                        put("image", editedImagePath)
                    }
                    call.resolve(ret)
                } else {
                    call.reject("Photo editing cancelled or failed.")
                }
            }
        }
    }


    @PluginMethod
    fun editPhoto(call: PluginCall) {
        val imagePath = call.getString("image")
        if (imagePath == null || imagePath.isEmpty()) {
            call.reject("Image path is required.")
            return
        }

        val intent = Intent(context, PhotoEditorActivity::class.java)
        intent.putExtra("imagePath", imagePath)
        savedCall = call
        launcher!!.launch(intent)
    }
}
