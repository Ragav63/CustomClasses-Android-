package com.example.customclass.cubeanimation

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.widget.FrameLayout
import android.widget.ImageView
import com.example.customclass.R

class CubeSliderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    companion object {
        const val LEFT = 3 // Swipe left -> Next image
        const val RIGHT = 4 // Swipe right -> Previous image
        private const val DURATION = 600L // Good duration for the effect
        private const val AUTO_SLIDE_INTERVAL = 4000L
    }

    // 🔑 We need TWO ImageViews
    private lateinit var mainImageView: ImageView
    private lateinit var nextImageView: ImageView

    private var imageResources: List<Int> = emptyList()
    private var currentImageIndex = 0
    private var isAnimating = false

    private val handler = Handler(Looper.getMainLooper())
    private var autoSlideEnabled = false

    private val autoSlideRunnable = Runnable { onAutoSlide() }

    init {
        initView()
        setupTouchListener()
    }

    private fun initView() {
        // Assuming view_cube_slider.xml has mainImageView and nextImageView
        View.inflate(context, R.layout.view_cube_slider, this)
        mainImageView = findViewById(R.id.mainImageView)
        nextImageView = findViewById(R.id.nextImageView)
        nextImageView.visibility = View.INVISIBLE // Start hidden
    }

    fun setImages(images: List<Int>) {
        imageResources = images
        if (images.isNotEmpty()) {
            mainImageView.setImageResource(images[0])
            nextImageView.visibility = View.INVISIBLE
        }
    }

    fun setAutoSlide(enabled: Boolean) {
        autoSlideEnabled = enabled
        if (enabled) {
            startAutoSlide()
        } else {
            stopAutoSlide()
        }
    }

    // ... (removed redundant setAnimationStyle and navigateTo for brevity) ...

    private fun setupTouchListener() {
        // Simple touch listener to start/stop auto slide
        setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    stopAutoSlide()
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    // Re-enable auto slide after the user interacts
                    if (autoSlideEnabled) startAutoSlide()
                    // Allow click events to pass (optional, depending on surrounding layout)
                    false
                }
                else -> false
            }
        }
    }

    // 🔑 Add a simple tap/click detection for swipe simulation
    // This is a minimal implementation, a proper GestureDetector is recommended for production.
    fun handleSwipeLeft() {
        onSwipeLeft()
    }

    fun handleSwipeRight() {
        onSwipeRight()
    }


    fun onSwipeLeft() {
        if (isAnimating || imageResources.isEmpty()) return
        val newIndex = (currentImageIndex + 1) % imageResources.size
        startAnimation(newIndex, LEFT)
    }

    fun onSwipeRight() {
        if (isAnimating || imageResources.isEmpty()) return
        val newIndex = if (currentImageIndex == 0) imageResources.size - 1 else currentImageIndex - 1
        startAnimation(newIndex, RIGHT)
    }

    private fun onAutoSlide() {
        if (imageResources.isEmpty() || isAnimating) return
        val newIndex = (currentImageIndex + 1) % imageResources.size
        startAnimation(newIndex, LEFT)
    }

    private fun startAnimation(newIndex: Int, direction: Int) {
        if (isAnimating) return
        isAnimating = true

        mainImageView.clearAnimation()
        nextImageView.clearAnimation()

        val outView = mainImageView
        val inView = nextImageView

        inView.setImageResource(imageResources[newIndex])
        inView.visibility = View.VISIBLE

        val outAnimation = CubeAnimation.create(direction, false, DURATION)
        val inAnimation = CubeAnimation.create(direction, true, DURATION)

        outAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                // 1. Hide the view that just finished animating out.
                outView.visibility = View.INVISIBLE

                // 2. Perform the swap of references.
                val temp = mainImageView
                mainImageView = nextImageView
                nextImageView = temp

                // 3. CRITICAL: Clear animations and final transformation state.
                // This ensures the newly visible view is static and ready.
                mainImageView.clearAnimation()
                mainImageView.animation = null // Explicitly remove the animation object

                nextImageView.clearAnimation()
                nextImageView.animation = null // Explicitly remove the animation object

                currentImageIndex = newIndex
                isAnimating = false

                if (autoSlideEnabled) {
                    startAutoSlide()
                }
            }
            override fun onAnimationRepeat(animation: Animation) {}
        })

        // Start both animations
        outView.startAnimation(outAnimation)
        inView.startAnimation(inAnimation)
    }

    private fun startAutoSlide() {
        handler.removeCallbacks(autoSlideRunnable)
        handler.postDelayed(autoSlideRunnable, AUTO_SLIDE_INTERVAL)
    }

    private fun stopAutoSlide() {
        handler.removeCallbacks(autoSlideRunnable)
    }

    fun cleanup() {
        stopAutoSlide()
        handler.removeCallbacksAndMessages(null)
    }
}