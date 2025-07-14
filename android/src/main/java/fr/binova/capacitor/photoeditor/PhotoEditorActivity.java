package fr.binova.capacitor.photoeditor;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

import ja.burhanrashid52.photoeditor.PhotoEditor;
import ja.burhanrashid52.photoeditor.PhotoEditorView;

public class PhotoEditorActivity extends AppCompatActivity {

    private PhotoEditor photoEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_editor);

        String imagePath = getIntent().getStringExtra("imagePath");
        Uri imageUri = Uri.parse(imagePath);

        PhotoEditorView photoEditorView = findViewById(R.id.photo_editor_view);
        ImageView sourceImage = photoEditorView.getSource();
        sourceImage.setImageURI(imageUri);

        photoEditor = new PhotoEditor.Builder(this, photoEditorView)
                .setPinchTextScalable(true)
                .build();

        Button btnSave = findViewById(R.id.btn_save);
        btnSave.setOnClickListener(v -> {
            File file = new File(getCacheDir(), "edited_" + System.currentTimeMillis() + ".jpg");
            photoEditor.saveAsFile(file.getAbsolutePath(), new PhotoEditor.OnSaveListener() {
                @Override
                public void onSuccess(@androidx.annotation.NonNull String imagePath) {
                    Intent intent = new Intent();
                    intent.putExtra("editedImagePath", imagePath);
                    setResult(RESULT_OK, intent);
                    finish();
                }

                @Override
                public void onFailure(@androidx.annotation.NonNull Exception e) {
                    setResult(RESULT_CANCELED);
                    finish();
                }
            });
        });
    }
}
