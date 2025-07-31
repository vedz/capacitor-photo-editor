package fr.binova.capacitor.photoeditor

import android.app.Dialog
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import fr.binova.capacitor.photoeditor.databinding.FragmentTextModalBinding
import ja.burhanrashid52.photoeditor.TextStyleBuilder

private const val ARG_TEXT = "text"
private const val ARG_TARGET_TAG = "target_tag"
private const val ARG_COLOR = "color"
private const val ARG_BACKGROUND_COLOR = "background_color"

class TextModal : DialogFragment() {
    private var text: String?= null
    private var targetTag: String?=null
    private var color: Int?=null
    private var background_color: Int?=null


    private lateinit var binding: FragmentTextModalBinding
//    private lateinit var textViewModel: TextViewModel
    private lateinit var textStyleBuider : TextStyleBuilder

    private var mTextEditorListener: TextEditorListener? = null

    interface TextEditorListener {
        fun onDone(inputText: String, textStyleBuilder: TextStyleBuilder)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            text = it.getString(ARG_TEXT)
            targetTag= it.getString(ARG_TARGET_TAG)
            color= it.getInt(ARG_COLOR)
            background_color= it.getInt(ARG_BACKGROUND_COLOR)
        }

        textStyleBuider = TextStyleBuilder()
        if(color !== null){
            textStyleBuider.withTextColor(color!!)
            textStyleBuider.withBackgroundColor(background_color!!)
        }else{
            textStyleBuider.withTextColor(Color.BLACK)
            textStyleBuider.withTextSize(24f)
            textStyleBuider.withGravity(3)
            textStyleBuider.withTextFont(Typeface.DEFAULT)
            textStyleBuider.withBackgroundColor(Color.TRANSPARENT)
            textStyleBuider.withTextAppearance(144)
        }

        setStyle(STYLE_NORMAL, R.style.AppTheme_Dialog_DialogThemeFullscreen)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity()
//        textViewModel = ViewModelProvider(activity).get(TextViewModel::class.java)
        binding.saveButton.setOnClickListener {
            saveText()
        }
        binding.text.addTextChangedListener {
            binding.textPreview.text = it.toString()
        }


        binding.toolIconColor.setOnClickListener {
            (activity as? PhotoEditorActivity)?.openColorPickerDialog { color ->
                val icon = binding.toolIconColor
                val backgroundDrawable = icon.background as GradientDrawable
                backgroundDrawable.setColor(color)
                textStyleBuider.withTextColor(color)
                binding.textPreview.setTextColor(color)
            }
        }
        binding.toolIconBackground.setOnClickListener {
            (activity as? PhotoEditorActivity)?.openColorPickerDialog { color ->
                val icon = binding.toolIconBackground
                val backgroundDrawable = icon.background as GradientDrawable
                backgroundDrawable.setColor(color)
                textStyleBuider.withBackgroundColor(color)
                binding.textPreview.setBackgroundColor(color)
            }
        }

    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTextModalBinding.inflate(inflater, container, false)

        if (text !== null) {
            binding.title.setText("Éditer")
            binding.text.setText(text)
            binding.textPreview.setText(text)
        }
        if (color !== null){
            binding.textPreview.setTextColor(color!!)
            binding.textPreview.setBackgroundColor(background_color!!)
            (binding.toolIconColor.background as GradientDrawable).setColor(color!!)
            (binding.toolIconBackground.background as GradientDrawable).setColor(background_color!!)

        }else{

            binding.textPreview.setTextColor(Color.BLACK)
            (binding.toolIconBackground.background as GradientDrawable).setColor(Color.TRANSPARENT)
            (binding.toolIconColor.background as GradientDrawable).setColor(Color.BLACK)
        }

        binding.text.requestFocus()
        return binding.root
    }


    private fun saveText(){

        val newText = binding.text.text.toString()
//        textViewModel.text.value = newText

        if(newText.isEmpty()){
            Toast.makeText(requireActivity(), "Le champ est vide", Toast.LENGTH_SHORT).show()
            return
        }
        binding.text.setText("")
        if (text !== null){
            val textEditorListener = mTextEditorListener
//            if (newText.isNotEmpty() && textEditorListener != null) {
//                textEditorListener.onDone(newText, textStyleBuider)
//            }
        }else{

            (activity as? PhotoEditorActivity)?.photoEditor?.addText(newText, textStyleBuider)
        }
        dismiss()
    }
    //Callback to listener if user is done with text editing
    fun setOnTextEditorListener(textEditorListener: TextEditorListener) {
        mTextEditorListener = textEditorListener
    }
    companion object {
        @JvmStatic
        fun newInstance(text: String, colorCode: Int, backgroundColorCode: Int ) =
            TextModal().apply {
                arguments = Bundle().apply {
                    putString(ARG_TEXT, text)
                    putInt(ARG_COLOR, colorCode)
                    putInt(ARG_BACKGROUND_COLOR, backgroundColorCode)
                }
            }
    }
}