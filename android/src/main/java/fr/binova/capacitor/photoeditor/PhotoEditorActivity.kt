package fr.binova.capacitor.photoeditor

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import ja.burhanrashid52.photoeditor.PhotoEditor
import ja.burhanrashid52.photoeditor.PhotoEditorView
import ja.burhanrashid52.photoeditor.shape.ShapeBuilder
import ja.burhanrashid52.photoeditor.shape.ShapeType
import java.io.File
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.nfc.Tag
import android.os.Environment
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.ColorPickerView
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import com.skydoves.colorpickerview.preference.ColorPickerPreferenceManager
import com.skydoves.colorpickerview.sliders.AlphaSlideBar
import com.skydoves.colorpickerview.sliders.BrightnessSlideBar
import ja.burhanrashid52.photoeditor.OnPhotoEditorListener
import ja.burhanrashid52.photoeditor.SaveSettings
import ja.burhanrashid52.photoeditor.TextStyleBuilder
import ja.burhanrashid52.photoeditor.ViewType
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import fr.binova.capacitor.photoeditor.databinding.ActivityPhotoEditorBinding
import androidx.core.graphics.drawable.toDrawable
import androidx.lifecycle.lifecycleScope
import ja.burhanrashid52.photoeditor.SaveFileResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileOutputStream
import java.io.IOException
import java.net.URI

class PhotoEditorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPhotoEditorBinding
    public lateinit var photoEditor: PhotoEditor
    private lateinit var imagePath: String

    private lateinit var photoEditorView: PhotoEditorView
    private lateinit var mainMenuBar: View
    private lateinit var subMenuContainer: View
    private lateinit var bottomControlBar: View
    private lateinit var cancelButton: View
    private lateinit var doneButton: View
    private lateinit var selectedToolText: TextView
    private lateinit var mSaveFileHelper: FileSaveHelper

    private var currentTool: String? = null
    private var mProgressDialog: AlertDialog? = null

    private var currentColor = Color.BLACK
    var currentBrushSize = 10f

    var shapeBuilder: ShapeBuilder =
        ShapeBuilder().withShapeColor(Color.BLACK).withShapeSize(50f).withShapeType(
            ShapeType.Oval
        )


    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showExitConfirmationDialog()
            }
        })

        binding = ActivityPhotoEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        photoEditorView = findViewById<PhotoEditorView>(R.id.photo_editor_view)
        imagePath = intent.getStringExtra("imagePath") ?: run {
            setResult(RESULT_CANCELED)
            finish()
            return
        }

        // No scaling or scrolling needed as the PhotoEditorView now takes all available space
        // and will automatically maintain the image's aspect ratio
        mainMenuBar = findViewById(R.id.main_menu_bar)
        subMenuContainer = findViewById(R.id.sub_menu_container)
        bottomControlBar = findViewById(R.id.bottom_control_bar)
        cancelButton = findViewById(R.id.btn_cancel)
        doneButton = findViewById(R.id.btn_done)
        selectedToolText = findViewById(R.id.selected_tool_name)



        findViewById<SeekBar>(R.id.brush_size_seekbar).setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                val brushSize = if (progress < 1) 1f else progress.toFloat()
                shapeBuilder.withShapeSize(brushSize)
                photoEditor.setShape(shapeBuilder)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }
        })
        currentColor = Color.BLACK

        if (!hasReadPermission()) {
            requestReadPermission()
        }
        loadImage()

        cancelButton.setOnClickListener {
            showExitConfirmationDialog()
        }

        doneButton.setOnClickListener {
            try {
                saveImageTestAsync(
                    onSaved = { file ->
                        Toast.makeText(this, "Image sauvegardée avec succès !", Toast.LENGTH_LONG)
                            .show()
                        val resultIntent = Intent()
                        resultIntent.putExtra("editedImagePath", file.absolutePath)
                        setResult(RESULT_OK, resultIntent)
                        finish()
                    },
                    onError = { error ->
                        Log.e("ERREUR", "Une erreur est survenue", error)
                        Toast.makeText(this, "Une erreur est survenue", Toast.LENGTH_LONG).show()
                    }
                )
            }
            catch (e: SecurityException) {
                Log.e("ERREUR", "Permissions manquantes",)
            }
        }

        findViewById<View>(R.id.undo).setOnClickListener {
            photoEditor.undo()
        }
        findViewById<View>(R.id.redo).setOnClickListener {
            photoEditor.redo()
        }
        showDrawSubmenu()


        mSaveFileHelper = FileSaveHelper(this)

    }


    private fun showExitConfirmationDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Quitter ?")
            .setMessage("Voulez-vous vraiment quitter cette page ?")
            .setPositiveButton("Oui") { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .setNegativeButton("Non") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(true)
            .show()
    }


    private fun showDrawSubmenu() {
        photoEditor.setBrushDrawingMode(true)
        val subMenuContainer = findViewById<LinearLayout>(R.id.main_menu_container)
        val subMenuTools = findViewById<LinearLayout>(R.id.sub_menu_tools)
        subMenuContainer.removeAllViews()

        val inflater = LayoutInflater.from(this)

        // Crée les boutons pour les outils
        val tools = listOf(
            Pair("Texte", R.drawable.ic_text),
            Pair("Stylo", R.drawable.ic_pen),
            Pair("Ligne", R.drawable.ic_line),
            Pair("Rectangle", R.drawable.ic_rectangle),
            Pair("Cercle", R.drawable.ic_cercle),
            Pair("Flèche", R.drawable.ic_arrow),
            Pair("Gomme", R.drawable.ic_eraser)
        )

        val colorItemTool = inflater.inflate(R.layout.color_item_tool, subMenuContainer, false)

        colorItemTool.setOnClickListener {
            openColorPickerDialog { color ->
                val icon = colorItemTool.findViewById<ImageView>(R.id.tool_icon_color)
                val backgroundDrawable = icon.background as GradientDrawable
                backgroundDrawable.setColor(color)
                shapeBuilder.withShapeColor(color)
                photoEditor.setShape(shapeBuilder)
            }
        }
        subMenuContainer.addView(colorItemTool)


        tools.forEach { (name, iconRes) ->
            val toolView = inflater.inflate(R.layout.item_tool_button, subMenuContainer, false)

            val icon = toolView.findViewById<ImageView>(R.id.tool_icon)
            val label = toolView.findViewById<TextView>(R.id.tool_label)

            icon.setImageResource(iconRes)
            label.text = name
            toolView.setOnClickListener {
                currentTool = name
                binding.selectedToolName.text = name
                when (name) {
                    "Texte" -> {
                        photoEditor.setBrushDrawingMode(false)
                        subMenuTools.visibility = View.GONE
                        TextModal().show(supportFragmentManager, "textEdit")
                        binding.selectedToolName.text = ""
                    }

                    "Stylo" -> {
                        shapeBuilder.withShapeType(ShapeType.Brush)
                        photoEditor.setBrushDrawingMode(true)
                        subMenuTools.visibility = View.VISIBLE
                        photoEditor.setShape(shapeBuilder)

                    }

                    "Ligne" -> {
                        shapeBuilder.withShapeType(ShapeType.Line)
                        photoEditor.setBrushDrawingMode(true)
                        photoEditor.setShape(shapeBuilder)
                        subMenuTools.visibility = View.VISIBLE
                    }

                    "Rectangle" -> {
                        shapeBuilder.withShapeType(ShapeType.Rectangle)
                        photoEditor.setBrushDrawingMode(true)
                        photoEditor.setShape(shapeBuilder)
                        subMenuTools.visibility = View.VISIBLE
                    }

                    "Cercle" -> {
                        shapeBuilder.withShapeType(ShapeType.Oval)
                        photoEditor.setBrushDrawingMode(true)
                        photoEditor.setShape(shapeBuilder)
                        subMenuTools.visibility = View.VISIBLE
                    }

                    "Flèche" -> {
                        shapeBuilder.withShapeType(ShapeType.Arrow())
                        photoEditor.setBrushDrawingMode(true)
                        photoEditor.setShape(shapeBuilder)
                        subMenuTools.visibility = View.VISIBLE
                    }

                    "Gomme" -> {
                        subMenuTools.visibility = View.GONE
                        photoEditor.brushEraser()
                        subMenuTools.visibility = View.VISIBLE
                    }
                }
            }

            subMenuContainer.addView(toolView)
        }
//        updateBottomBar()
    }

    fun openColorPickerDialog(onColorSelected: (Int) -> Unit) {
        val dialogView = layoutInflater.inflate(R.layout.color_picker_dialog, null)
        val dialog = MaterialAlertDialogBuilder(this, R.style.CustomDialogTheme)
            .setView(dialogView)
            .create()
        dialog.window?.setBackgroundDrawable(Color.WHITE.toDrawable())

        val colorPickerView = dialogView.findViewById<ColorPickerView>(R.id.colorPickerView)
        val btnApply = dialogView.findViewById<Button>(R.id.btn_apply_color)
        val colorPreview = dialogView.findViewById<View>(R.id.color_preview)
        val bgDrawable = colorPreview.background as GradientDrawable
        val alphaSlider = dialogView.findViewById<AlphaSlideBar>(R.id.alphaSlider)
        val brightnessSlider = dialogView.findViewById<BrightnessSlideBar>(R.id.brightnessSlider)
        val predefinedContainer =
            dialogView.findViewById<LinearLayout>(R.id.predefinedColorsContainer)
        colorPickerView.attachAlphaSlider(alphaSlider)
        colorPickerView.attachBrightnessSlider(brightnessSlider)
        colorPickerView.setPreferenceName("MyColorPicker")
        colorPickerView.setLifecycleOwner(this)

        var selectedColor = currentColor
        bgDrawable.setColor(currentColor)

        colorPickerView.setColorListener(object : ColorEnvelopeListener {
            override fun onColorSelected(envelope: ColorEnvelope, fromUser: Boolean) {
                selectedColor = envelope.color
                bgDrawable.setColor(selectedColor)
            }
        })


        // Liste de couleurs prédéfinies
        val predefinedColors = listOf(
            Color.BLACK, Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW,
            Color.CYAN, Color.MAGENTA, Color.WHITE, Color.GRAY
        )

        predefinedColors.forEach { colorInt ->
            val colorView = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(120, 120).apply {
                    setMargins(8, 0, 8, 0)
                }
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(colorInt)
                    setStroke(4, if (colorInt == Color.WHITE) Color.DKGRAY else Color.WHITE)
                }
                setOnClickListener {
                    selectedColor = colorInt
                    bgDrawable.setColor(selectedColor)
                }
            }
            predefinedContainer.addView(colorView)
        }

        btnApply.setOnClickListener {
            onColorSelected(selectedColor)
            currentColor = selectedColor
            ColorPickerPreferenceManager.getInstance(this).saveColorPickerData(colorPickerView);
            dialog.dismiss()
        }
        dialog.show()
    }

    fun View.fadeIn(duration: Long = 250) {
        this.apply {
            alpha = 0f
            visibility = View.VISIBLE
            animate().alpha(1f).setDuration(duration).start()
        }
    }

    fun View.fadeOut(duration: Long = 250) {
        this.animate().alpha(0f).setDuration(duration).withEndAction {
            this.visibility = View.GONE
        }.start()
    }

    private fun loadImage() {
        val imageUri = Uri.parse(imagePath)
        val photoEditorView = findViewById<PhotoEditorView>(R.id.photo_editor_view)
        photoEditorView.source.setImageURI(imageUri)

        photoEditor = PhotoEditor.Builder(this, photoEditorView)
            .setPinchTextScalable(true)
            .setClipSourceImage(true)
            .build()

        shapeBuilder = ShapeBuilder()

        photoEditor.setOnPhotoEditorListener(object : OnPhotoEditorListener {
            override fun onAddViewListener(
                viewType: ViewType,
                numberOfAddedViews: Int
            ) {
            }

            override fun onEditTextChangeListener(
                rootView: View,
                text: String,
                colorCode: Int
            ) {
                val inputTextView = rootView.findViewById<TextView>(R.id.tvPhotoEditorText)
                val backgroundColor = when (val bg = inputTextView.background) {
                    is ColorDrawable -> bg.color
                    is GradientDrawable -> {
                        try {
                            val field = GradientDrawable::class.java.getDeclaredField("mFillPaint")
                            field.isAccessible = true
                            val paint = field.get(bg) as Paint
                            paint.color
                        } catch (e: Exception) {
                            Color.TRANSPARENT
                        }
                    }

                    else -> Color.TRANSPARENT
                }
                var modal = TextModal.newInstance(text, colorCode, backgroundColor)
                modal.setOnTextEditorListener(object :
                    TextModal.TextEditorListener {
                    override fun onDone(
                        inputText: String,
                        textStyleBuilder: TextStyleBuilder
                    ) {
                        photoEditor.editText(rootView, inputText, textStyleBuilder)
                    }
                })
                modal.show(supportFragmentManager, "textEdit")

            }

            override fun onRemoveViewListener(
                viewType: ViewType,
                numberOfAddedViews: Int
            ) {
            }

            override fun onStartViewChangeListener(viewType: ViewType) {
            }

            override fun onStopViewChangeListener(viewType: ViewType) {
            }

            override fun onTouchSourceImage(event: MotionEvent) {
            }

        })

    }

    protected fun showLoading(message: String) {
        val progressBar = ProgressBar(this).apply {
            isIndeterminate = true
        }

        val layout = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(10, 30, 10, 30)
            addView(progressBar)

            val textView = TextView(this@PhotoEditorActivity).apply {
                text = message
                setPadding(30, 0, 0, 0)
            }
            addView(textView)
        }

        mProgressDialog = MaterialAlertDialogBuilder(this)
            .setCancelable(false)
            .setView(layout)
            .create()

        mProgressDialog?.show()
    }


    protected fun hideLoading() {
        mProgressDialog?.dismiss()
        mProgressDialog = null
    }


    @RequiresPermission(allOf = [Manifest.permission.WRITE_EXTERNAL_STORAGE])
    private fun saveImageTestAsync(onSaved: (File) -> Unit, onError: (Throwable) -> Unit = {}) {
        val hasStoragePermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        if (hasStoragePermission || FileSaveHelper.isSdkHigherThan28()) {
            showLoading("Sauvegarde...")

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val originalFile = File(URI(imagePath))
                    val directory = originalFile.parentFile ?: throw IOException("Chemin invalide")
                    val file = File(directory, "edited_${System.currentTimeMillis()}.jpg")

                    val finalBitmap = withContext(Dispatchers.Main) {
                        photoEditor.saveCustom()
                    }

                    val savedFile =
                        saveBitmapToFile(this@PhotoEditorActivity, finalBitmap, file.absolutePath)

                    withContext(Dispatchers.Main) {
                        hideLoading()
                        onSaved(savedFile)
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        hideLoading()
                        onError(e)
                    }
                }
            }
        } else {
            Log.e("ERREUR", "Une erreur est survenue")
            Toast.makeText(this, "Une erreur est survenue", Toast.LENGTH_LONG).show()
        }
    }


    @RequiresPermission(allOf = [Manifest.permission.WRITE_EXTERNAL_STORAGE])
    private fun saveImageTest() {
        val hasStoragePermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        if (hasStoragePermission || FileSaveHelper.isSdkHigherThan28()) {
            showLoading("Enregistrement...")

            val originalFile = File(URI(imagePath))
            val directory = originalFile.parentFile ?: throw IOException("Invalid path")
            val file = File(directory, "edited_${System.currentTimeMillis()}.jpg")

            val finalBitmap = photoEditor.saveCustom() // ton image finale haute résolution

            val result = saveBitmapToFile(this, finalBitmap, file.absolutePath)
            hideLoading()
        } else {
//            requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    fun saveBitmapToFile(
        context: Context,
        bitmap: Bitmap,
        filePath: String,
        format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG,
        quality: Int = 90
    ): File {
        val file = File(filePath)
        file.parentFile?.mkdirs()

        FileOutputStream(file).use { output ->
            bitmap.compress(format, quality, output)
            output.flush()
        }

        return file
    }


    private fun saveImage() {
        try {
            val originalFile = File(imagePath)
            val directory = originalFile.parentFile ?: throw IOException("Invalid path")
            val file = File(directory, "edited_${System.currentTimeMillis()}.jpg")
            val settings = SaveSettings.Builder()
                .setClearViewsEnabled(true)
                .setTransparencyEnabled(true)
                .build()
            photoEditor.saveAsFile(
                file.absolutePath,
                settings,
                object : PhotoEditor.OnSaveListener {
                    override fun onSuccess(imagePath: String) {
                        val resultIntent = Intent().apply {
                            putExtra("editedImagePath", imagePath)
                        }
                        setResult(RESULT_OK, resultIntent)
                        finish()
                    }

                    override fun onFailure(e: Exception) {
                        setResult(RESULT_CANCELED)
                        finish()
                    }
                })
        } catch (e: SecurityException) {
            // Permission manquante
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    private fun hasReadPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) ==
                    PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestReadPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                REQUEST_READ_EXTERNAL
            )
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_READ_EXTERNAL
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            REQUEST_READ_EXTERNAL -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadImage()
                } else {
                    setResult(RESULT_CANCELED)
                    finish()
                }
            }

            REQUEST_WRITE_EXTERNAL -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    saveImage()
                } else {
                    setResult(RESULT_CANCELED)
                    finish()
                }
            }
        }
    }

    companion object {
        private const val REQUEST_WRITE_EXTERNAL = 1001
        private const val REQUEST_READ_EXTERNAL = 1002
    }
}
