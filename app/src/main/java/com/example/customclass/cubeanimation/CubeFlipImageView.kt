package com.example.customclass.cubeanimation

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.example.customclass.databinding.ViewCubeImageBinding
import java.util.Timer
import kotlin.concurrent.schedule

class CubeFlipImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding =
        ViewCubeImageBinding.inflate(LayoutInflater.from(context), this, true)

    private var flipDuration = 600L
    private var isFrontVisible = true
    private var images: List<ImageItem> = emptyList()
    private var currentIndex = 0
    private var autoFlipTimer: Timer? = null
    private var autoFlipInterval = 3000L

    /**
     * Provide a list of images with optional overlays.
     */
    fun setImageList(items: List<ImageItem>) {
        images = items
        currentIndex = 0
        if (images.isNotEmpty()) {
            loadFrontImage(images[0])
            if (images.size > 1) {
                loadBackImage(images[1 % images.size])
            }
        }
    }

    private fun loadFrontImage(item: ImageItem) {
        Glide.with(context).load(item.imageUrl).into(binding.frontImage)
        bindOverlay(binding.frontText, binding.frontButton, item)
    }

    private fun loadBackImage(item: ImageItem) {
        Glide.with(context).load(item.imageUrl).into(binding.backImage)
        bindOverlay(binding.backText, binding.backButton, item)
    }

    private fun bindOverlay(textView: android.widget.TextView,
                            button: android.widget.Button,
                            item: ImageItem) {
        textView.isVisible = !item.overlayText.isNullOrEmpty()
        textView.text = item.overlayText
        // Position text
        val lp = textView.layoutParams as LayoutParams
        lp.gravity = when (item.textPosition) {
            OverlayPosition.TOP -> android.view.Gravity.TOP or android.view.Gravity.CENTER_HORIZONTAL
            OverlayPosition.CENTER -> android.view.Gravity.CENTER
            OverlayPosition.BOTTOM -> android.view.Gravity.BOTTOM or android.view.Gravity.CENTER_HORIZONTAL
        }
        textView.layoutParams = lp

        button.isVisible = !item.overlayButtonLabel.isNullOrEmpty()
        button.text = item.overlayButtonLabel
        button.setOnClickListener { item.overlayButtonAction?.invoke() }
    }

    /**
     * Manual flip forward
     */
    fun flipNext() {
        if (images.size < 2) return
        val nextIndex = (currentIndex + 1) % images.size
        flipToIndex(nextIndex)
    }

    /**
     * Manual flip backward
     */
    fun flipPrevious() {
        if (images.size < 2) return
        val prevIndex = if (currentIndex - 1 < 0) images.size - 1 else currentIndex - 1
        flipToIndex(prevIndex)
    }

    private fun flipToIndex(nextIndex: Int) {
        val (visibleView, invisibleView) =
            if (isFrontVisible) binding.frontContainer to binding.backContainer
            else binding.backContainer to binding.frontContainer

        // Prepare invisible side with next image
        val nextItem = images[nextIndex]
        if (isFrontVisible) loadBackImage(nextItem) else loadFrontImage(nextItem)

        visibleView.animate()
            .rotationY(90f)
            .setDuration(flipDuration / 2)
            .setInterpolator(DecelerateInterpolator())
            .withEndAction {
                visibleView.isVisible = false
                invisibleView.rotationY = -90f
                invisibleView.isVisible = true
                invisibleView.animate()
                    .rotationY(0f)
                    .setDuration(flipDuration / 2)
                    .setInterpolator(DecelerateInterpolator())
                    .start()
                currentIndex = nextIndex
            }
            .start()

        isFrontVisible = !isFrontVisible
    }

    fun setFlipDuration(durationMillis: Long) {
        flipDuration = durationMillis
    }

    /** -------- Auto Flip ---------- */
    fun startAutoFlip(intervalMillis: Long = 3000L) {
        autoFlipInterval = intervalMillis
        stopAutoFlip() // clear old timer
        autoFlipTimer = Timer()
        autoFlipTimer?.schedule(autoFlipInterval, autoFlipInterval) {
            post { flipNext() }
        }
    }

    fun stopAutoFlip() {
        autoFlipTimer?.cancel()
        autoFlipTimer = null
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopAutoFlip()
    }
}

data class ImageItem(
    val imageUrl: String,
    val overlayText: String? = null,
    val overlayButtonLabel: String? = null,
    val overlayButtonAction: (() -> Unit)? = null,
    val textPosition: OverlayPosition = OverlayPosition.CENTER
)

enum class OverlayPosition { TOP, CENTER, BOTTOM }
