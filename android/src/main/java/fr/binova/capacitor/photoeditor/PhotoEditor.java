package fr.binova.capacitor.photoeditor;

import android.app.Activity;
import android.content.Intent;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.JSObject;

@CapacitorPlugin(name = "PhotoEditor")
public class PhotoEditor extends Plugin {

    private ActivityResultLauncher<Intent> launcher;
    private PluginCall savedCall;

    @Override
    public void load() {
        launcher = bridge.registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (savedCall == null) return;
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    String editedImagePath = result.getData().getStringExtra("editedImagePath");
                    JSObject ret = new JSObject();
                    ret.put("image", editedImagePath);
                    savedCall.resolve(ret);
                } else {
                    savedCall.reject("Photo editing cancelled or failed.");
                }
            }
        );
    }

    @PluginMethod
    public void editPhoto(PluginCall call) {
        String imagePath = call.getString("image");
        if (imagePath == null || imagePath.isEmpty()) {
            call.reject("Image path is required.");
            return;
        }

        Intent intent = new Intent(getContext(), PhotoEditorActivity.class);
        intent.putExtra("imagePath", imagePath);
        savedCall = call;
        launcher.launch(intent);
    }
}
