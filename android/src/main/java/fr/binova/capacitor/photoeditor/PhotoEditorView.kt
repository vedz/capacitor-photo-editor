package fr.binova.capacitor.photoeditor

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.RelativeLayout
import fr.binova.capacitor.photoeditor.FilterImageView.OnImageChangedListener
import androidx.core.graphics.scale

/**
 *
 *
 * This ViewGroup will have the [DrawingView] to draw paint on it with [ImageView]
 * which our source image
 *
 *
 * @author [Burhanuddin Rashid](https://github.com/burhanrashid52)
 * @version 0.1.1
 * @since 1/18/2018
 */
class PhotoEditorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : RelativeLayout(context, attrs, defStyle) {

    private var mImgSource: FilterImageView = FilterImageView(context)

    internal var drawingView: DrawingView
        private set

    private var mImageFilterView: ImageFilterView
    private var clipSourceImage = false

    private var isSettingImageBitmap = false
    internal var originalBitmap: Bitmap? = null

    init {
        val sourceParam = setupImageSource(attrs)
        mImageFilterView = ImageFilterView(context)
        val filterParam = setupFilterView()

        mImgSource.setOnImageChangedListener(object : FilterImageView.OnImageChangedListener {
            override fun onBitmapLoaded(sourceBitmap: Bitmap?) {
                if (sourceBitmap == null || isSettingImageBitmap) return
                isSettingImageBitmap = true

                val bitmapWidth = sourceBitmap.width
                val bitmapHeight = sourceBitmap.height

                val screenWidth = resources.displayMetrics.widthPixels
                val screenHeight = resources.displayMetrics.heightPixels

                val scale = minOf(
                    screenWidth.toFloat() / bitmapWidth,
                    screenHeight.toFloat() / bitmapHeight,
                    1f
                )

                val targetWidth = (bitmapWidth * scale).toInt()
                val targetHeight = (bitmapHeight * scale).toInt()

                mImgSource.setImageDrawable(null)

                val layoutParams = LayoutParams(targetWidth, targetHeight).apply {
                    addRule(CENTER_IN_PARENT, TRUE)
                }
                mImgSource.layoutParams = layoutParams
                mImageFilterView.layoutParams = layoutParams
                drawingView.layoutParams = layoutParams

                originalBitmap = sourceBitmap
                val scaledBitmap = sourceBitmap.scale(targetWidth, targetHeight)
                mImgSource.setImageBitmap(scaledBitmap)

                mImageFilterView.requestLayout()
                drawingView.requestLayout()

                isSettingImageBitmap = false
            }
        })


        drawingView = DrawingView(context)
        val brushParam = setupDrawingView()

        addView(mImgSource, sourceParam)
        addView(mImageFilterView, filterParam)
        addView(drawingView, brushParam)
    }

    @SuppressLint("Recycle")
    private fun setupImageSource(attrs: AttributeSet?): LayoutParams {
        mImgSource.id = imgSrcId
        mImgSource.adjustViewBounds = false
        mImgSource.scaleType = ImageView.ScaleType.FIT_XY
        attrs?.let {
            val a = context.obtainStyledAttributes(it, R.styleable.PhotoEditorView)
            val imgSrcDrawable = a.getDrawable(R.styleable.PhotoEditorView_photo_src)
            if (imgSrcDrawable != null) {
                mImgSource.setImageDrawable(imgSrcDrawable)
            }
        }

        var widthParam = ViewGroup.LayoutParams.MATCH_PARENT
        if (clipSourceImage) {
            widthParam = ViewGroup.LayoutParams.WRAP_CONTENT
        }
        val params = LayoutParams(
            widthParam, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.addRule(CENTER_IN_PARENT, TRUE)
        return params
    }

    private fun setupDrawingView(): LayoutParams {
        drawingView.visibility = GONE
        drawingView.id = shapeSrcId

        // Align drawing view to the size of image view
        val params = LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.addRule(CENTER_IN_PARENT, TRUE)
        params.addRule(ALIGN_TOP, imgSrcId)
        params.addRule(ALIGN_BOTTOM, imgSrcId)
        params.addRule(ALIGN_LEFT, imgSrcId)
        params.addRule(ALIGN_RIGHT, imgSrcId)
        return params
    }

    private fun setupFilterView(): LayoutParams {
        mImageFilterView.visibility = GONE
        mImageFilterView.id = glFilterId

        //Align brush to the size of image view
        val params = LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.addRule(CENTER_IN_PARENT, TRUE)
        params.addRule(ALIGN_TOP, imgSrcId)
        params.addRule(ALIGN_BOTTOM, imgSrcId)
        return params
    }

    /**
     * Source image which you want to edit
     *
     * @return source ImageView
     */
    val source: ImageView
        get() = mImgSource

    internal suspend fun saveFilter(): Bitmap {
        return if (mImageFilterView.visibility == VISIBLE) {
            val saveBitmap = try {
                mImageFilterView.saveBitmap()
            } catch (t: Throwable) {
                throw RuntimeException("Couldn't save bitmap with filter", t)
            }
            mImgSource.setImageBitmap(saveBitmap)
            mImageFilterView.visibility = GONE
            saveBitmap
        } else {
            mImgSource.bitmap!!
        }
    }

    internal fun setFilterEffect(filterType: PhotoFilter) {
        mImageFilterView.visibility = VISIBLE
        mImageFilterView.setFilterEffect(filterType)
    }

    internal fun setFilterEffect(customEffect: CustomEffect?) {
        mImageFilterView.visibility = VISIBLE
        mImageFilterView.setFilterEffect(customEffect)
    }

    internal fun setClipSourceImage(clip: Boolean) {
        clipSourceImage = clip
        val param = setupImageSource(null)
        mImgSource.layoutParams = param
    } // endregion

    companion object {
        private const val imgSrcId = 1
        private const val shapeSrcId = 2
        private const val glFilterId = 3

    }
}